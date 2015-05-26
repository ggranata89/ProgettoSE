package com.example.ricky.mycity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;

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

    public final static String USER_DETAILS = "com.example.ricky.mycity.USER_DETAILS";
    public final static String USER_IMAGE = "com.example.ricky.mycity.USER_IMAGE";

    private String sessid, session_name, token, user, img_url,uid;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
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

                session_name = jsonObj.getString("session_name");
                sessid = jsonObj.getString("sessid");
                token = jsonObj.getString("token");
                user = jsonObj.getString("user");

                JSONObject userObj = new JSONObject(user);
                String str = userObj.getString("picture");
                uid = userObj.getString("uid");
                //String picture = jsonObj.getString("picture");
                JSONObject jsonObject1 = new JSONObject(str);
                img_url = jsonObject1.getString("url");
                System.out.println("FROM LOGIN " + img_url);
                Log.d("FROM LOGIN", img_url);

            }catch (Exception e){
                e.printStackTrace();
            }

            return 0;
        }

        protected void onPostExecute(Integer result){
            if(session_name != null && sessid != null) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                /*intent.putExtra("session_name", session_name);
                intent.putExtra("sessid", sessid);
                intent.putExtra("token", token);
                intent.putExtra(USER_IMAGE, img_url);
                intent.putExtra(USER_DETAILS, user);*/
                SharedPreferences.Editor editor = getSharedPreferences("user_details",MODE_PRIVATE).edit();
                editor.putString("session_name",session_name);
                editor.putString("sessid",sessid);
                editor.putString("token",token);
                editor.putString("user_image",img_url);
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
        if(activeNetwork != null && activeNetwork.isConnected())
            return true;

        return false;
    }

    public void doLoginButton(View view){
        if(showStatusConnection())
            new doLogin().execute();
        else {
            //Toast.makeText(getApplicationContext(),"Connessione Assente", Toast.LENGTH_LONG).show();
            Dialog dialog = new Dialog(this,"Connessione Assente", "Impossibile contattare il server. Controlla la connessione e riprova.");
            dialog.show();
        }

    }

    public void doRegisterButton(View view) {
        // new doRegister().execute();
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

        startActivity(intent);

    }

    private class getLogin extends AsyncTask<Void,Void,String> {
        String session_name,sessid,token;
        String web_service_token;

        SharedPreferences user_details = getSharedPreferences("user_details",MODE_PRIVATE);

        public getLogin() {
            session_name = user_details.getString("session_name", "");
            sessid = user_details.getString("sessid","");
            token = user_details.getString("token","");
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(TOKEN_URI);

            try{
                httpPost.setHeader("Content-Type","application/json");
                httpPost.setHeader("Cookie",session_name+"="+sessid);
                HttpResponse httpResponse = httpClient.execute(httpPost);

                web_service_token = EntityUtils.toString(httpResponse.getEntity());


            }catch(Exception e){
                e.printStackTrace();
            }
            return web_service_token;
        }

        protected  void onPostExecute(String ws_token){
            Log.v("MyToken",token);
            Log.v("web_service_token",ws_token);
            if(token.equals(ws_token)){
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
            else
                setContentView(R.layout.activity_login);

        }
    }
}
