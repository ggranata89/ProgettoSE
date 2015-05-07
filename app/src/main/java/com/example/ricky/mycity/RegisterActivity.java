package com.example.ricky.mycity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gc.materialdesign.widgets.Dialog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by giuseppe on 05/05/15.
 */
public class RegisterActivity extends ActionBarActivity implements Costanti{

    private String sessid, session_name,token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class doSend extends AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(REGISTER_URI);

            try {
                EditText username = (EditText) findViewById(R.id.reg_username);
                EditText password = (EditText) findViewById(R.id.reg_password);
                EditText email = (EditText) findViewById(R.id.reg_email);

                Log.d("RegisterActivity", "New Registration request: " + username + " " + password + " " + email);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", username.getText().toString().trim());
                jsonObject.put("pass", password.getText().toString().trim());
                jsonObject.put("mail", email.getText().toString().trim());
                jsonObject.put("status", "1");

                StringEntity stringEntity = new StringEntity(jsonObject.toString());
                Log.d("RegisterActivity", "JSON inviato: " +stringEntity);
                stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                //Bisogna inserire il token ma non so perchè funziona
                httpPost.setEntity(stringEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);

                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());

                JSONObject jsonObj = new JSONObject(jsonResponse);
                Log.d("RegisterActivity",jsonResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

        protected void onPostExecute(Integer result) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

            startActivity(intent);
            Toast.makeText(getApplicationContext(),"Registrazione effettuata con successo", Toast.LENGTH_LONG).show();
            //Dialog dialog = new Dialog(,"Registrazione effettuata", "Entra in MyCity con le tue credenziali.");
            //dialog.show();
        }
    }

    public void doSendButton(View view){
        new doSend().execute();
    }
}
