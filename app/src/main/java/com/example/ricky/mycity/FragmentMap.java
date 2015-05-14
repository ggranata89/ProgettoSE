package com.example.ricky.mycity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ricky on 2/20/15.
 */
public class FragmentMap extends Fragment {

    private GoogleMap googleMap;
    private MapView mapView;
    private double latitude, longitude;
    private String title, user, body, date, priority, category;
    private Long timestamp;


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

        Log.d("FRAGMENT MAP LATITUDE", "latitude: " + latitude);
        Log.d("FRAGMENT MAP LONGITUDE", "longitude: " + longitude);

        googleMap = mapView.getMap();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.75, 15.00), 10));
        googleMap.setMyLocationEnabled(true);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("My Current Position"));

        //Async task to load location from webservice
        new doLoadLocation().execute();
        return rootView;
    }


    private class doLoadLocation extends AsyncTask<Void, Void, HashMap<String, Report>> implements Costanti {
        public HashMap<String, Report> reportMap = new HashMap<>();
        Report report = null;

        @Override
        protected HashMap<String, Report> doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(REPORT_URI);
            String jsonResponse;

            double latitude;
            double longitude;
            try {

                HttpResponse httpResponse = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                Log.d("MainActivity-JSON", jsonResponse);
                JSONArray jsonArray = new JSONArray(jsonResponse);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    title = jsonObject.getString("title");
                    body = jsonObject.getJSONObject("body").getJSONArray("und").getJSONObject(0).getString("value");
                    timestamp = Long.parseLong(jsonObject.getString("created"));
                    date = convertTimestampToDate(timestamp);

                    priority = jsonObject.getJSONObject("field_priority").getJSONArray("und").getJSONObject(0).getString("value");
                    priority = getPriorityByIndex(priority);

                    category = jsonObject.getJSONObject("field_priority").getJSONArray("und").getJSONObject(0).getString("value");
                    category = getCategoryByIndex(category);

                    JSONObject jsonLocation = jsonObject.getJSONObject("location");
                    latitude = Double.parseDouble(jsonLocation.getString("latitude"));
                    longitude = Double.parseDouble(jsonLocation.getString("longitude"));
                    user = jsonObject.getString("name");
                    reportMap.put(title, new Report(title, body, new LatLng(latitude, longitude), priority, category, user, date));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return reportMap;
        }

        public String getPriorityByIndex(String priority) {
            switch (priority) {
                case "1":
                    return "NONE";
                case "2":
                    return "MINOR";
                case "3":
                    return "NORMAL";
                case "4":
                    return "CRITICAL";
                default:
                    return "NONE";
            }
        }

        public String getCategoryByIndex(String category) {
            switch (category) {
                case "1":
                    return "WASTE_MANAGEMENT";
                case "2":
                    return "ROUTINE_MAINTENANCE";
                case "3":
                    return "ROAD_SIGN";
                case "4":
                    return "VANDALISM";
                case "5":
                    return "ILLEGAL_BILLPOSTING";
                case "6":
                    return "OTHER";
                default:
                    return "NONE";
            }
        }

        public String convertTimestampToDate(Long timestamp) {

            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();//get your local time zone.
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            sdf.setTimeZone(tz);//set time zone.
            String localTime = sdf.format(timestamp * 1000);
            Date date = new Date();
            try {
                date = sdf.parse(localTime);//get local date
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return String.valueOf(date);
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

                            Iterator iterator = reportMap.keySet().iterator();

                            while (iterator.hasNext()) {
                                String key = iterator.next().toString();
                                report = (Report) reportMap.get(key);

                                if (marker.getPosition().latitude == report.getLocation().latitude && marker.getPosition().longitude == report.getLocation().longitude) {

                                    tvLat.setText("Latitude: " + report.getLocation().latitude);
                                    tvLng.setText("Longitude: " + report.getLocation().longitude);
                                    tvDescription.setText("Description: " + report.getBody());
                                    tvCategory.setText("Category: " + report.getCategory());
                                    tvPriority.setText("Priority: " + report.getPriority());
                                    tvUser.setText("User: " + report.getUser());
                                    tvDate.setText("Date: " + report.getDate());
                                }
                            }
                            return v;
                        }
                    });
                    googleMap.addMarker(markerOptions).showInfoWindow();
                }
            }
        }
   }
}



