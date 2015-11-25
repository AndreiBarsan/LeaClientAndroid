package eu.cs_syd.leaclient;

import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonSchemaUtils {

  public String readTextAsset(AssetManager assets, String path) throws IOException {
    try (InputStream rIs = assets.open(path);
         BufferedReader br = new BufferedReader(new InputStreamReader(rIs))) {
      String content = "";
      String buffer;
      while (null != (buffer = br.readLine())) {
        content += buffer;
      }

      return content;
    }
  }

  public void schemaTest(AssetManager assets) {
    try {
      String requestSchemaText = readTextAsset(assets, "json/request.json");
      String replySchemaText = readTextAsset(assets, "json/reply.json");

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

    } catch(JSONException e) {
      e.printStackTrace();
      Log.wtf("assets", "Could not parse JSON schema(s).");
    } catch (IOException e) {
      e.printStackTrace();
      Log.wtf("assets", "Could not read JSON schema(s).");
    }
  }
}
