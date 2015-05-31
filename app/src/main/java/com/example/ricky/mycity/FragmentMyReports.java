package com.example.ricky.mycity;

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class FragmentMyReports extends Fragment implements Costanti {
    private ListView listView;
    private String uid;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_reports, container, false);
        listView = (ListView) rootView.findViewById(R.id.listview);
        SharedPreferences user_details = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        uid = user_details.getString("uid","");
        new FillTable().execute();


        return rootView;
    }

    private class FillTable extends AsyncTask<Void,Void,HashMap<String,Report>> {
        final ArrayList<Report> list = new ArrayList<Report>();
        @Override
        protected HashMap<String,Report> doInBackground(Void... params) {
            HashMap<String,Report> myReportsMap = new HashMap<>();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(REPORT_URI);
            String jsonResponse;
            try {

                HttpResponse httpResponse = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                JSONArray jsonArray = new JSONArray(jsonResponse);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONParser jsonParser = new JSONParser(jsonObject);
                    Report r = jsonParser.getReportFromJSON();
                    if(r.getUid().equals(uid))
                        myReportsMap.put(r.getTitle(),r);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return myReportsMap;
        }

        protected void  onPostExecute(HashMap<String,Report> myReports){
            if(!myReports.isEmpty()){
                Report report = null;
                Iterator iterator = myReports.keySet().iterator();
                while(iterator.hasNext()){
                    String key = iterator.next().toString();
                    list.add(myReports.get(key));

                }
            }
            final AdapterReport adapter = new AdapterReport(getActivity(),list);
            listView.setAdapter(adapter);

        }
    }
}