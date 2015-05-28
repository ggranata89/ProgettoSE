package com.example.ricky.mycity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class LoginActivity extends ActionBarActivity implements Costanti{

    private String sessid, sessionName, token, user, imgUrl,uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new getLogin().execute();
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

                sessionName = jsonObj.getString("session_name");
                sessid = jsonObj.getString("sessid");
                token = jsonObj.getString("token");
                user = jsonObj.getString("user");

                JSONObject userObj = new JSONObject(user);
                String str = userObj.getString("picture");
                uid = userObj.getString("uid");
                JSONObject jsonObject1 = new JSONObject(str);
                imgUrl = jsonObject1.getString("url");
                System.out.println("FROM LOGIN " + imgUrl);
                Log.d("FROM LOGIN", imgUrl);

            }catch (Exception e){
                e.printStackTrace();
            }

            return 0;
        }

        protected void onPostExecute(Integer result){
            if(sessionName != null && sessid != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                SharedPreferences.Editor editor = getSharedPreferences("userDetails",MODE_PRIVATE).edit();
                editor.putString("session_name",sessionName);
                editor.putString("sessid",sessid);
                editor.putString("token",token);
                editor.putString("user_image",imgUrl);
                editor.putString("user",user);
                editor.putString("uid",uid);
                editor.commit();

                startActivity(intent);
            }else{
                Toast toast = Toast.makeText(getApplicationContext(),"Autenticazione non riuscita", Toast.LENGTH_LONG);
                toast.show();
            }

        }

    }

    private boolean showStatusConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetwork != null && wifiNetwork.isConnected())
            return true;

        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(mobileNetwork != null && mobileNetwork.isConnected())
            return true;

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();

    }

    public void doLoginButton(View view){
        if(showStatusConnection())
            new doLogin().execute();
        else {
            Dialog dialog = new Dialog(this,"Connessione Assente", "Impossibile contattare il server. Controlla la connessione e riprova.");
            dialog.show();
        }

    }

    public void doRegisterButton(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private class getLogin extends AsyncTask<Void,Void,String> {
        String webServiceToken = null;

        SharedPreferences userDetails = getSharedPreferences("userDetails",MODE_PRIVATE);

        public getLogin() {
            sessionName = userDetails.getString("session_name", "");
            sessid = userDetails.getString("sessid","");
            token = userDetails.getString("token","");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(TOKEN_URI);

            try{
                httpPost.setHeader("Content-Type","application/json");
                httpPost.setHeader("Cookie",sessionName+"="+sessid);
                HttpResponse httpResponse = httpClient.execute(httpPost);

                webServiceToken = EntityUtils.toString(httpResponse.getEntity());


            }catch(Exception e){
                e.printStackTrace();
            }
            return webServiceToken;
        }

        protected  void onPostExecute(String wsToken){
            if(token.equals(wsToken)){
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
            else
                setContentView(R.layout.activity_login);

        }
    }
}
