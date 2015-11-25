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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
      String requestSchema = readTextAsset("json/request.json");
      String replySchema = readTextAsset("json/reply.json");


    } catch (IOException e) {
      e.printStackTrace();
      Log.wtf("Assets", "Could not read JSON schema(s).");
    }

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
}
