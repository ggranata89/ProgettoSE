package com.example.ricky.mycity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class FragmentMap extends Fragment {

    private GoogleMap googleMap;
    private MapView mapView;
    private double latitude, longitude;
    private ImageView ivImage;
    private int lastVid = 0;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle args = getArguments();
        latitude = args.getDouble("latitude", 0.0);
        longitude = args.getDouble("longitude", 0.0);

        googleMap = mapView.getMap();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10));
        googleMap.setMyLocationEnabled(true);

        //Async task to load location from webservice
        new doLoadLocation().execute();
        return rootView;
    }


    private class doLoadLocation extends AsyncTask<Void, Void, HashMap<String, Report>> implements Costanti {
        public HashMap<String, Report> reportMap = new HashMap<>();
        Report report = null;

        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Caricamento segnalazioni in corso...");
            progressDialog.show();
        }

        @Override
        protected HashMap<String, Report> doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(REPORT_URI);
            String jsonResponse;

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                JSONArray jsonArray = new JSONArray(jsonResponse);
                Log.d("FROM FRAGMENT MAP", "JSON RESPONSE: " + jsonResponse);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if(i==0){
                        lastVid = jsonObject.getInt("vid");
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences("userDetails",getActivity().MODE_PRIVATE).edit();
                        editor.putInt("currentVid",lastVid);
                        editor.commit();
                    }
                    JSONParser jsonParser = new JSONParser(jsonObject);
                    Report r = jsonParser.getReportFromJSON();
                    reportMap.put(r.getTitle(),r);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return reportMap;
        }



        protected void onPostExecute(final HashMap reportMap) {

            if (!this.reportMap.isEmpty()) {
                Iterator iterator = reportMap.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next().toString();
                    report = (Report) reportMap.get(key);
                    final MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(report.getLocation());

                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            // Getting view from the layout file info_window_layout
                            View v = getActivity().getLayoutInflater().inflate(R.layout.info_window_layout, null);
                            // Getting the position from the marker
                            LatLng latLng = marker.getPosition();

                            TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                            TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                            TextView tvDescription = (TextView) v.findViewById(R.id.tv_report_description);
                            TextView tvCategory = (TextView) v.findViewById(R.id.tv_report_category);
                            TextView tvPriority = (TextView) v.findViewById(R.id.tv_report_priority);
                            TextView tvUser = (TextView) v.findViewById(R.id.tv_user);
                            TextView tvDate = (TextView) v.findViewById(R.id.tv_report_date);
                            ivImage = (ImageView) v.findViewById(R.id.iv_image);
                            Bitmap bitmap = null;

                            Iterator iterator = reportMap.keySet().iterator();

                            while (iterator.hasNext()) {
                                String key = iterator.next().toString();
                                report = (Report) reportMap.get(key);

                                if (marker.getPosition().latitude == report.getLocation().latitude && marker.getPosition().longitude == report.getLocation().longitude) {
                                    try {
                                        bitmap = new GetImage().execute("http://46.101.148.74/sites/default/files/" + report.getImage()).get();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }

                                    tvLat.setText("Latitude: " + report.getLocation().latitude);
                                    tvLng.setText("Longitude: " + report.getLocation().longitude);
                                    tvDescription.setText("Description: " + report.getBody());
                                    tvCategory.setText("Category: " + report.getCategory());
                                    tvPriority.setText("Priority: " + report.getPriority());
                                    tvUser.setText("User: " + report.getUser());
                                    tvDate.setText("Date: " + report.getDate());
                                    ivImage.setImageBitmap(bitmap);
                                }
                            }

                            return v;
                        }
                    });
                    googleMap.addMarker(markerOptions).showInfoWindow();
                }
            }
            progressDialog.dismiss();
        }
   }

    private class GetImage extends AsyncTask<String, Void, Bitmap>{


        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(strings[0]);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ivImage.setImageBitmap(bitmap);
        }
    }
}



