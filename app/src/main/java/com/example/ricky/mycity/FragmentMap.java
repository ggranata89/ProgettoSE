package com.example.ricky.mycity;

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
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ricky on 2/20/15.
 */
public class FragmentMap extends Fragment {

    private GoogleMap googleMap;
    private MapView mapView;
    private double latitude, longitude, latitudeMarker, longitudeMarker;
    private String title, location;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try{
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }

        Bundle args = getArguments();
        latitude = args.getDouble("latitude", 0.0);
        longitude= args.getDouble("longitude", 0.0);

        Log.d("FRAGMENT MAP LATITUDE", "latitude: " + latitude);
        Log.d("FRAGMENT MAP LONGITUDE", "longitude: " + longitude);

        googleMap = mapView.getMap();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.75, 15.00), 10));
        googleMap.setMyLocationEnabled(true);
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("My Current Position"));
        new LoadMarkers().execute();
        return rootView;
    }

    public class LoadMarkers extends AsyncTask<Void, Void, String> implements Costanti{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //TODO
        }

        @Override
        protected String doInBackground(Void... arg0) {
            URL url;
            StringBuffer sb = new StringBuffer();
            try {
                url = new URL(REPORT_URI);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                Log.d("FROM BACKGROUND", "Response Code: " + connection.getResponseCode());
                InputStream iStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
                String line;
                while( (line = reader.readLine()) != null){
                    sb.append(line);
                    Log.d("FROM LOAD", line);
                }

                reader.close();
                iStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("FROM LOAD MARKERS", sb.toString());
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            parserMarker(result);
            //new ParserTask().execute(result);
        }

        public void parserMarker(String string){
            JSONArray reports = null;
            JSONObject locationObject = null;
            try {
                reports = new JSONArray(string);
                for(int i = 0; i<reports.length(); ++i) {
                    title = reports.getJSONObject(i).getString("title");
                    location = reports.getJSONObject(i).getString("location");
                    locationObject = new JSONObject(location);
                    latitudeMarker = locationObject.getDouble("latitude");
                    longitudeMarker = locationObject.getDouble("longitude");
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitudeMarker, longitudeMarker))
                            .title(title));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
