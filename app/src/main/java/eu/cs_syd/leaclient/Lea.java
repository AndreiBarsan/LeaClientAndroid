package eu.cs_syd.leaclient;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lea implements Closeable {

  public static class LeaException extends RuntimeException {
    public LeaException(String detailMessage) {
      super(detailMessage);
    }

    public LeaException(String detailMessage, Throwable throwable) {
      super(detailMessage, throwable);
    }

    public LeaException(Throwable throwable) {
      super(throwable);
    }
  }

  public static class LeaWorker implements Runnable {

    private String host;
    private int port;
    private Charset charset;
    private int socketTimeoutMs;
    private BlockingQueue<String> jobs;

    public LeaWorker(String host, int port, Charset charset, int socketTimeoutMs, BlockingQueue<String> jobs) {
      this.host = host;
      this.port = port;
      this.charset = charset;
      this.socketTimeoutMs = socketTimeoutMs;
      this.jobs = jobs;
    }

    @Override
    public void run() {
      Log.d("network", "Going to connect to Lea.");
      try {
        workerLoop();
      } catch(LeaException e) {
        Log.e("LeaWorker", "Exception in Lea worker.", e);
      }
    }

    private void workerLoop() {
      try(Socket socket = new Socket(host, port)) {
        Log.d("network", "Connected to Lea!");
        // TODO(andrei) Try-with resources with these dudes as well.
        InputStream socketIn = socket.getInputStream();
        OutputStream socketOut = socket.getOutputStream();
        socket.setSoTimeout(socketTimeoutMs);

        InputStreamReader inputReader = new InputStreamReader(socketIn, charset);
        OutputStreamWriter outputWriter = new OutputStreamWriter(socketOut, charset);

        while(!Thread.currentThread().isInterrupted()) {
          try {
            Log.d("LeaWorker", "Waiting for a job.");
            String nextJob = jobs.take();
            Log.d("LeaWorker", "Got a new job: " + nextJob);
            sendCommand(inputReader, outputWriter, nextJob);
          } catch (InterruptedException e) {
            Log.w("Lea", "Worker interrupted. Stopping worker loop.", e);
            break;
          }
        }

        Log.d("LeaWorker", "Stopped command loop.");
      }
      catch(ConnectException e) {
        throw new LeaException("Could not establish connection to Lea.", e);
      }
      catch(SocketTimeoutException e) {
        throw new LeaException("Timed out waiting for Lea.", e);
      }
      catch(SocketException e) {
        throw new LeaException("Received socket exception connecting to Lea.", e);
      }
      catch(UnknownHostException e) {
        throw new LeaException("Unknown Lea host: " + this.host, e);
      }
      catch(IOException e) {
        throw new LeaException("General IOException in Lea.", e);
      }
    }

    private void sendCommand(InputStreamReader inputReader, OutputStreamWriter outputWriter, String jsonCommand) throws IOException {
      Log.d("LeaWorker", "sendCommand: " + jsonCommand);
      JsonFactory jsf = new JsonFactory();
      JsonParser parser = jsf.createParser(inputReader);
      ObjectMapper mapper = new ObjectMapper();

      Log.d("LeaWorker", "Going to write JSON command.");
      outputWriter.write(jsonCommand);
      outputWriter.flush();

      boolean finished = false;
      while (!finished) {
        Log.d("LeaWorker", "Going to read JSON...");
        LeaResponse resp = mapper.readValue(parser, LeaResponse.class);
        Log.d("json", "Lea resp " + resp);
        Log.d("json", "Lea reply type: " + resp.reply_type);
        Log.d("json", "Lea reply string:" + resp.output_str);
        Log.d("json", "Delay: " + resp.output_delay);

        if (resp.reply_type.equals("done")) {
          Log.d("json", "We are done.");
          finished = true;
        } else {
          Log.d("json", "More to come...");
        }
      }
    }
  }

  private LeaWorker mainWorker;
  private BlockingQueue<String> jobQueue;
  private ExecutorService executorService;

  // TODO(andrei) Automatic server detection.
  public Lea(String host, int port) {
    this.jobQueue = new ArrayBlockingQueue<String>(10);
    this.mainWorker = new LeaWorker(host, port, StandardCharsets.UTF_8, 5000, jobQueue);
    this.executorService = Executors.newCachedThreadPool();
    executorService.submit(mainWorker);
  }

  public void sendCommand(String jsonCommand) {
    try {
      Log.d("Lea", "Putting job in command queue: " + jsonCommand);
      jobQueue.put(jsonCommand);
    } catch (InterruptedException e) {
      Log.w("Lea", "Unexpected interruption.", e);
    }
  }

  @Override
  public void close() throws IOException {
    // This will attempt to interrupt all threads in the pool, automatically causing the active
    // LeaWorker(s) to close their sockets nicely.
    this.executorService.shutdownNow();
  }
}
