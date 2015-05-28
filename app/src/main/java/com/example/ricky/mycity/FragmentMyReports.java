package com.example.ricky.mycity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Iterator;


public class FragmentMyReports extends Fragment implements Costanti {
    private TableLayout tableLayout;
    private String uid;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_reports, container, false);
        tableLayout = (TableLayout) rootView.findViewById(R.id.TableLayout);
        SharedPreferences user_details = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        uid = user_details.getString("uid","");
        new FillTable().execute();


        return rootView;
    }

    private class FillTable extends AsyncTask<Void,Void,HashMap<String,Report>> {
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
                    report = myReports.get(key);
                    TableRow row = new TableRow(getActivity());
                    TableRow.LayoutParams params1 =new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT,1.0f);
                    TableRow.LayoutParams params2=new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    params1.setMargins(2,2,2,2);
                    TextView txt1 = new TextView(getActivity());
                    TextView txt2 = new TextView(getActivity());
                    TextView txt3 = new TextView(getActivity());
                    txt1.setText(report.getTitle());
                    txt2.setText(report.getBody());
                    txt3.setText(report.getDate());
                    txt1.setLayoutParams(params1);
                    txt2.setLayoutParams(params1);
                    txt3.setLayoutParams(params1);
                    row.addView(txt1);
                    row.addView(txt2);
                    row.addView(txt3);
                    row.setLayoutParams(params2);
                    row.setBackgroundResource(android.R.drawable.edit_text);
                    if(report.getStatus().equals("Open"))
                        row.setBackgroundColor(Color.rgb(255,204,204));
                    else row.setBackgroundColor(Color.rgb(204,255,204));
                    tableLayout.addView(row);
               }
            }

        }
    }
}