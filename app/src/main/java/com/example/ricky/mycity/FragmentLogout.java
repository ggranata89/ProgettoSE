package com.example.ricky.mycity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class FragmentLogout extends Fragment implements Costanti{
    private String sessid,session_name,token;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("Logout Section");
        SharedPreferences user_details = this.getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        sessid = user_details.getString("sessid","");
        session_name = user_details.getString("session_name","");
        token = user_details.getString("token", "");
        new doLogout().execute();
        return rootView;
    }

    private class doLogout extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String jsonResponse="false";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(LOGOUT_URI);

                httpPost.setHeader("Content-Type","application/json");
                httpPost.setHeader("Cookie",session_name+"="+sessid);
                httpPost.setHeader("X-CSRF-Token",token);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                jsonResponse = EntityUtils.toString(httpResponse.getEntity());
            }catch(Exception e){
                e.printStackTrace();
            }

            return jsonResponse.substring(1,jsonResponse.length()-1);
        }
        protected void onPostExecute(String response){
            SharedPreferences.Editor user_details = getActivity().getSharedPreferences("userDetails",Context.MODE_PRIVATE).edit();
            user_details.clear();
            user_details.commit();
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
        }
    }
}
