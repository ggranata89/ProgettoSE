package com.example.ricky.mycity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;

import static com.example.ricky.mycity.Costanti.MINIMUM_DISTANCE_CHANGE_FOR_UPDATES;
import static com.example.ricky.mycity.Costanti.MINIMUM_TIME_BETWEEN_UPDATES;

public class MainActivity extends MaterialNavigationDrawer implements MaterialAccountListener,Costanti {

    private String name, mail, img_url;
    private ProgressDialog pDialog;
    private Bitmap bitmap;
    private MaterialAccount myAccount;
    private LocationManager locationManager = null;
    private double latitudine, longitudine;
    private FragmentMap fragmentMap = new FragmentMap();
    private FragmentReport fragmentReport = new FragmentReport();
    private boolean isGPSEnabled, isNetworkEnabled;
    private Location location;
    private int lastVid = 0;
    private MaterialSection mappaSection;
    private Intent intent = new Intent();
    //MyReceiver myReceiver;
    //IntentFilter intentFilter;
    //private final IntentFilter intentFilter = new IntentFilter(CUSTOM_INTENT);

    @Override
    public void init(Bundle savedInstanceState){
        intent.setAction("com.example.ricky.mycity.MyReceiver.show_toast");
        sendBroadcast(intent);

        getMyLocation();

        new GetLastVid().execute();

        SharedPreferences user_details = getSharedPreferences("userDetails",MODE_PRIVATE);

        String user = user_details.getString("user","");
        img_url = user_details.getString("user_image","");

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(user);
            name = jsonObject.getString("name");
            mail = jsonObject.getString("mail");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(img_url!=null)
            new LoadImage().execute(img_url);

        //Account
        myAccount = new MaterialAccount(this.getResources(), name, mail, R.drawable.default_img, R.drawable.background);
        this.addAccount(myAccount);
        mappaSection = newSection(getString(R.string.mappa_segnalazione), R.mipmap.map, fragmentMap);
        //mappaSection.setNotificationsText("4+");
        this.setAccountListener(this);
        this.addSection(newSection(getString(R.string.invia_segnalazione), R.mipmap.send_now, fragmentReport).setSectionColor(Color.parseColor("#03a9f4")));
        this.addSection(mappaSection);
        this.addSection(newSection(getString(R.string.mie_segnalazioni), R.mipmap.ic_action_view_as_list, new FragmentMyReports()).setSectionColor(Color.parseColor("#60b39f")));
        this.addDivisor();
        this.addSection(newSection(getString(R.string.profile), R.mipmap.profile, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.info), R.mipmap.info, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.aiuto), R.mipmap.aiuto, new FragmentButton()).setSectionColor(Color.parseColor("#03a9f4")));
        this.addSection(newSection(getString(R.string.logout), R.mipmap.ic_action_remove, new FragmentLogout()).setSectionColor(Color.parseColor("#03a9f4")));

        this.addSubheader(getAppVersion());

        this.addBottomSection(newSection(getString(R.string.bottom), R.mipmap.settings, new Intent(this, Settings.class)));
        allowArrowAnimation();
    }

    private class GetLastVid extends AsyncTask<Void, Void, Integer> implements Costanti {
        public HashMap<String, Report> reportMap = new HashMap<>();
        Report report = null;

        @Override
        protected Integer doInBackground(Void... params) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(REPORT_URI);
            String jsonResponse;

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                JSONArray jsonArray = new JSONArray(jsonResponse);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                lastVid = jsonObject.getInt("vid");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return lastVid;
        }

        @Override
        protected void onPostExecute(Integer result){
            SharedPreferences user_details = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            int vid = user_details.getInt("currentVid", 0);
            if(vid == 0){
                mappaSection.setNotificationsText("10+"); //assegnazione statica per test
            }
            else
                if(lastVid > vid)
                    mappaSection.setNotificationsText((lastVid-vid)+"+");
        }
    }

    protected void onResume(){
        new GetLastVid().execute();
        Log.d("FROM ON RESUME", "SONO DENTRO ONRESTART");
        //registerReceiver(myReceiver, intentFilter);
        super.onResume();
    }

    private void getMyLocation() {
        //sendBroadcast(new Intent(CUSTOM_INTENT));
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MINIMUM_TIME_BETWEEN_UPDATES,
                        MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitudine = location.getLatitude();
                        longitudine = location.getLongitude();
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MINIMUM_TIME_BETWEEN_UPDATES,
                            MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
                    Log.d("GPS", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitudine = location.getLatitude();
                            longitudine = location.getLongitude();
                        }
                    }
                }
            }
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitudine);
            bundle.putDouble("longitude", longitudine);
            fragmentMap.setArguments(bundle);
            fragmentReport.setArguments(bundle);
        }
    }

    private String getAppVersion(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "\u00A9  MyCity versione " + packageInfo.versionName;
    }

    @Override
    public void onAccountOpening(MaterialAccount account){
        //TODO
    }

    @Override
    public void onChangeAccount(MaterialAccount account){
        //TODO
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading Image ....");
            pDialog.show();

        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                myAccount.setPhoto(bitmap);
                pDialog.dismiss();

            }else{
                pDialog.dismiss();
                //Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onPause() {
        //unregisterReceiver(myReceiver);
        sendBroadcast(intent);
        super.onPause();
    }

    /*@Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }*/

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            latitudine = location.getLatitude();
            longitudine = location.getLongitude();
        }

        public void onStatusChanged(String s, int i, Bundle b) {
            //TODO
        }

        public void onProviderDisabled(String s) {
            //TODO
        }
        public void onProviderEnabled(String s) {
            //TODO
        }
    }
}
