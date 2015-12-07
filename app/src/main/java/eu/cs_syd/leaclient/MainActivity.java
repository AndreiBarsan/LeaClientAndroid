package eu.cs_syd.leaclient;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
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

  static class LeaResponse {
    public String reply_type;
    public String output_str;
    public String output_delay;
  }

  // There is a ton of discussions to be had about the proper way to design this (e.g. we could
  // consider having Lea broadcast its address periodically; at the same time, this may not work
  // on all networks).
  private void connectToLea() {
  }

  private void commandLea(String command) {

  }

  private String nextCommandJson() {
    Map<String, String> dummyCommand = new HashMap<>();
    dummyCommand.put("request_type", "initial");
    String whatToSay = "sorry";
    dummyCommand.put("initial_args", "say " + whatToSay);
    JSONObject command = new JSONObject(dummyCommand);
    return command.toString();
  }
}
