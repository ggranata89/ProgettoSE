package com.example.ricky.mycity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

/**
 * Created by giuseppe on 10/05/15.
 */
public class ReportActivity extends ActionBarActivity implements Costanti {

    String sessid, session_name, token;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        new doReport().execute();

    }


    private class doReport extends AsyncTask<String, Integer, Integer> {
        ;
        protected Integer doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(NODE_URI);
            try{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "report");
                jsonObject.put("title", "Report by android");

                jsonObject.put("body", "report android sample");

                StringEntity stringEntity = new StringEntity(jsonObject.toString());
                Log.d(" JSON SEND", "JSON: " + jsonObject.toString());
                stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                stringEntity.setContentType(new BasicHeader("Cookie","SESSc9dea8e5917ef9291649e3fb4cf6a251=AC1KL6mogBBrdeLPZpnUm4AjH-_XjUpsZE0jZHaXgiI"));
                stringEntity.setContentType(new BasicHeader("X-CSRF-Token","JytfqIWswXGd9yQX35z8q0ACRlbD9b8JWOWBod49Prk"));

                httpPost.setEntity(stringEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);

                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                Log.d(" JSON RESPONE", "JSON: " + jsonResponse);


            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }




    }
}

