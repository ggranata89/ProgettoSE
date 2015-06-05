package com.example.ricky.mycity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends ActionBarActivity implements Costanti{

    EditText username, password, email,retypedPassword;
    private ProgressDialog progressDialog;

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

    private class doSend extends AsyncTask<String, Integer, String> {

        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Registrazione in corso");
            progressDialog.show();
        }

        protected String doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(REGISTER_URI);
            String response="";

            try {
                username = (EditText) findViewById(R.id.reg_username);
                password = (EditText) findViewById(R.id.reg_password);
                retypedPassword = (EditText) findViewById(R.id.retyped_password);
                email = (EditText) findViewById(R.id.reg_email);

                JSONObject jsonObject = new JSONObject();
                if(isEmpty(username) || isEmpty(password) || isEmpty(retypedPassword) || isEmpty(email))
                    response =  "Compilare tutti i campi";
                else if (!password.getText().toString().trim().equals(retypedPassword.getText().toString().trim())) {
                    response ="Le due password non corrispondono";
                }
                else{
                    jsonObject.put("name", username.getText().toString().trim());
                    jsonObject.put("pass", password.getText().toString().trim());
                    jsonObject.put("mail", email.getText().toString().trim());
                    jsonObject.put("status", "1");

                    StringEntity stringEntity = new StringEntity(jsonObject.toString());
                    Log.d("RegisterActivity", "JSON inviato: " + stringEntity);
                    stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                    httpPost.setEntity(stringEntity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    response = EntityUtils.toString(httpResponse.getEntity());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);



            try{

                JSONObject json = new JSONObject(result);
                if(json.has("uid"))
                    Toast.makeText(getApplicationContext(),"Registrazione effettuata con successo", Toast.LENGTH_LONG).show();
                else if(json.has("form_errors")) {
                    JSONObject JSONError = json.getJSONObject("form_errors");
                    if(JSONError.has("name"))
                        Toast.makeText(getApplicationContext(), "Username non valido o già in uso", Toast.LENGTH_LONG).show();
                    if(JSONError.has("mail"))
                        Toast.makeText(getApplicationContext(), "Email non valida o già in uso", Toast.LENGTH_LONG).show();
                }

            }catch(JSONException e){
                Toast.makeText(getApplicationContext(),result, Toast.LENGTH_LONG).show();
            }

        }

        private boolean isEmpty(EditText editText){
            return editText.getText().toString().trim().length()==0;
        }
    }

    public void doSendButton(View view){
        new doSend().execute();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        Log.d("BACK PRESSED REGISTER","REGISTER BACK PRESSED");
    }
}
