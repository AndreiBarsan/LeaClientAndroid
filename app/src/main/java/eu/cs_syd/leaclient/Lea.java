package eu.cs_syd.leaclient;

import android.util.Log;

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

public class Lea implements Closeable {

  private String host;
  private int port;
  private Charset charset;
  private int socketTimeoutMs;
  private Socket socket;

  // TODO(andrei) Automatic server detection.
  public Lea(String host, int port) {
    this.host = host;
    this.port = port;
    this.charset = StandardCharsets.UTF_8;
    this.socketTimeoutMs = 10000;

    Log.d("network", "Going to connect to Lea.");

    try {
      this.socket = new Socket(host, port);
      Log.d("network", "Connected to Lea!");
      InputStream socketIn = socket.getInputStream();
      OutputStream socketOut = socket.getOutputStream();
      socket.setSoTimeout(socketTimeoutMs);

      try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socketIn, charset));
        OutputStreamWriter osw = new OutputStreamWriter(socketOut, charset)

//        String jsonString = nextCommandJson();
//        osw.write(jsonString);
//        osw.flush();
//        JsonFactory jsf = new JsonFactory();
//        JsonParser parser = jsf.createParser(bufferedReader);
//        ObjectMapper mapper = new ObjectMapper();
//
//        boolean finished = false;
//        while(!finished) {
//          LeaResponse resp = mapper.readValue(parser, LeaResponse.class);
//          Log.d("json", "Lea resp " + resp);
//          Log.d("json", "Lea reply type: " + resp.reply_type);
//          Log.d("json", "Lea reply string:" + resp.output_str);
//
//          if(resp.reply_type.equals("done")) {
//            Log.d("json", "We are done.");
//            finished = true;
//          }
//          else {
//            Log.d("json", "More to come...");
//          }
//        }
    }
    catch(ConnectException e) {
      Log.e("network", "Could not establish connection to Lea.", e);
    }
    catch(SocketTimeoutException e) {
      Log.e("network", "Timed out waiting for Lea.", e);
    }
    catch(SocketException e) {
      Log.e("network", "Received socket exception connecting to Lea.", e);
    }
    catch(UnknownHostException e) {
      Log.e("network", "Unknown host: " + this.host, e);
    }
    catch(IOException e) {
      Log.wtf("network", "General IOException.", e);
    }

  }

  public void disconnect() {
    socket.close();
  }

  @Override
  public void close() throws IOException {
    this.disconnect();
  }
}
