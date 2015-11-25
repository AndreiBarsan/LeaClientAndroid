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
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  private String readTextAsset(String path) throws IOException {
    try (InputStream rIs = getAssets().open(path);
         BufferedReader br = new BufferedReader(new InputStreamReader(rIs))) {
      String content = "";
      String buffer;
      while (null != (buffer = br.readLine())) {
        content += buffer;
      }

      return content;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Lea Client");

    try {
      String requestSchemaText = readTextAsset("json/request.json");
      String replySchemaText = readTextAsset("json/reply.json");

      JSONObject requestSchema = new JSONObject(requestSchemaText);
      // Note: schema validation is probably not worth it, especially since no major libraries
      // exist to perform it.
      String sanityCheck = requestSchema
        .getJSONArray("oneOf")
        .getJSONObject(0)
        .getJSONObject("properties")
        .getJSONObject("request_type")
        .getJSONArray("enum")
        .getString(0);

      String scMessage = "Sanity check: " + sanityCheck;
      Log.d("assets", scMessage);
      Toast.makeText(getApplicationContext(), scMessage, Toast.LENGTH_LONG)
        .show();

    } catch(JSONException e) {
      e.printStackTrace();
      Log.wtf("assets", "Could not parse JSON schema(s).");
    } catch (IOException e) {
      e.printStackTrace();
      Log.wtf("assets", "Could not read JSON schema(s).");
    }

    String dummyJsonText = nextCommandJson();
    Log.d("derp", dummyJsonText);

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
    String leaName = "";
    Charset charset = StandardCharsets.UTF_8;
    int leaPort = 1000;
    try(Socket socket = new Socket(leaName, leaPort)) {
      OutputStream socketOut = socket.getOutputStream();
      try(OutputStreamWriter osw = new OutputStreamWriter(socketOut, charset)) {
        String jsonString = nextCommandJson();
        osw.write(jsonString);
      }
    }
    catch(SocketException e) {
      Log.e("network", "Received socket exception connecting to Lea.", e);
    }
    catch(UnknownHostException e) {
      Log.e("network", "Unknown host: " + leaName, e);
    }
    catch(IOException e) {
      Log.wtf("network", "General IOException.", e);
    }
  }

  private String nextCommandJson() {
    Map<String, String> dummyCommand = new HashMap<>();
    dummyCommand.put("request_type", "initial");
    dummyCommand.put("initial_args", "say hello");
    JSONObject command = new JSONObject(dummyCommand);
    return command.toString();
  }
}
