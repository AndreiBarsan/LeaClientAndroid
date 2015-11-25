package eu.cs_syd.leaclient;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Lea Client");

    String dummyJsonText = nextCommandJson();
    Log.d("sanityCheck", dummyJsonText);

    ExecutorService svc = Executors.newCachedThreadPool();
    svc.submit(new Runnable() {
      @Override
      public void run() {
        connectToLea();
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "This block will encode the JSON and fire it off to Lea",
          Snackbar.LENGTH_LONG).setAction("Action", null).show();

        String command = ((TextView) findViewById(R.id.commandInput)).getText().toString();

        Toast.makeText(
          getApplicationContext(),
          "Command: " + command,
          Toast.LENGTH_SHORT
        ).show();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // There is a ton of discussions to be had about the proper way to design this (e.g. we could
  // consider having Lea broadcast its address periodically; at the same time, this may not work
  // on all networks).
  private void connectToLea() {
    String leaName = "172.31.184.141";
    Charset charset = StandardCharsets.UTF_8;
    int socketTimeoutMs = 3000;
    int leaPort = 65432;
    Log.d("network", "Going to connect to Lea.");
    try(Socket socket = new Socket(leaName, leaPort)) {
      Log.d("network", "Connected to Lea!");
      InputStream socketIn = socket.getInputStream();
      OutputStream socketOut = socket.getOutputStream();
      socket.setSoTimeout(socketTimeoutMs);

      try(
        InputStreamReader isr = new InputStreamReader(socketIn, charset);
        BufferedReader bufferedReader = new BufferedReader(isr);
        OutputStreamWriter osw = new OutputStreamWriter(socketOut, charset)
      ) {
        boolean quit = false;
        while(!quit) {
          String jsonString = nextCommandJson();
          osw.write(jsonString);
          Log.d("network", "Sent payload to Lea.");
          Log.d("network", "Lea:" + socketIn.available());

          JSONObject response = readJson(bufferedReader);
          Log.d("json", "Lea's response: " + response);
          quit = true;
        }
      }
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
      Log.e("network", "Unknown host: " + leaName, e);
    }
    catch(JSONException e) {
      Log.e("json", "Lea be making no sense. Could not parse JSON.", e);
    }
    catch(IOException e) {
      Log.wtf("network", "General IOException.", e);
    }
  }

  private String nextCommandJson() {
    Map<String, String> dummyCommand = new HashMap<>();
    dummyCommand.put("request_type", "initial");
    dummyCommand.put("initial_args", "say this is a fun way to debug");
    JSONObject command = new JSONObject(dummyCommand);
    return command.toString();
  }

  private JSONObject readJson(BufferedReader inputReader) throws IOException, JSONException {
    Log.d("json", "Will now read from Lea.");
    String buff = "";
    String line = null;
    while(null != (line = inputReader.readLine())) {
      Log.d("json", "Lea line:\t" + line);
      buff += line;
    }
    Log.d("json", "Lea whole:\n" + buff);

    return new JSONObject(buff);
  }
}
