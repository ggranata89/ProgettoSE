package com.example.ricky.mycity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class LoginActivity extends ActionBarActivity implements Costanti{

    private String sessid, session_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    private class doLogin extends AsyncTask<String, Integer, Integer>{
        protected Integer doInBackground(String... params){

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(LOGIN_URI);

            try{
                EditText username = (EditText) findViewById(R.id.username);
                EditText password = (EditText) findViewById(R.id.password);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username.getText().toString().trim());
                jsonObject.put("password", password.getText().toString().trim());

                StringEntity stringEntity = new StringEntity(jsonObject.toString());
                stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(stringEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);

                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());

                JSONObject jsonObj = new JSONObject(jsonResponse);

                session_name = jsonObj.getString("session_name");
                sessid = jsonObj.getString("sessid");
            }catch (Exception e){
                e.printStackTrace();
            }

            return 0;
        }

        protected void onPostExecute(Integer result){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            intent.putExtra("session_name", session_name);
            intent.putExtra("sessid", sessid);

            startActivity(intent);
        }

    }

    public void doLoginButton(View view){
        new doLogin().execute();
    }
}
