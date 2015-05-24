package com.example.ricky.mycity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import static com.example.ricky.mycity.Costanti.*;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;

public class MainActivity extends MaterialNavigationDrawer implements MaterialAccountListener {

    private String name, mail, img_url;
    private ProgressDialog pDialog;
    private Bitmap bitmap;
    private MaterialAccount myAccount;
    private LocationManager locationManager = null;
    private double latitudine, longitudine;
    private FragmentMap fragmentMap = new FragmentMap();
    private boolean isGPSEnabled, isNetworkEnabled;
    private Location location;

    @Override
    public void init(Bundle savedInstanceState){

        getMyLocation();
        //Intent intent = getIntent();
        //String user = intent.getStringExtra(LoginActivity.USER_DETAILS);
        //img_url = intent.getStringExtra(LoginActivity.USER_IMAGE);

        SharedPreferences user_details = getSharedPreferences("user_details",MODE_PRIVATE);

        String user = user_details.getString("user","");
        img_url = user_details.getString("user_image","");

        JSONObject jsonObject = null;
        JSONObject jsonObject1 = null;
        try {
            jsonObject = new JSONObject(user);
            name = jsonObject.getString("name");
            mail = jsonObject.getString("mail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Log.d("FROM MAIN - URL", img_url);

        if(img_url!=null)
            new LoadImage().execute(img_url);

        //Log.d("MAIN BITMAP", bitmap.toString());
        //Account
        myAccount = new MaterialAccount(this.getResources(), name, mail, R.drawable.default_img, R.drawable.background);
        this.addAccount(myAccount);

        this.setAccountListener(this);
        this.addSection(newSection(getString(R.string.invia_segnalazione), R.mipmap.send_now, new FragmentReport()));
        this.addSection(newSection(getString(R.string.mappa_segnalazione), R.mipmap.map, fragmentMap));
        //this.addSubheader("Categoria");
        this.addDivisor();
        this.addSection(newSection(getString(R.string.profile), R.mipmap.profile, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.info), R.mipmap.info, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.aiuto), R.mipmap.aiuto, new FragmentButton()).setSectionColor(Color.parseColor("#03a9f4")));
        this.addSection(newSection(getString(R.string.logout), R.mipmap.credits, new FragmentLogout()).setSectionColor(Color.parseColor("#03a9f4")));

        this.addSubheader(getAppVersion());

        this.addBottomSection(newSection(getString(R.string.bottom), R.mipmap.settings, new Intent(this,Settings.class)));
    }

    public double getLatitude(){
        return latitudine;
    }

    public double getLongitude(){
        return longitudine;
    }

    private void getMyLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        /*locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                new MyLocationListener());*/

        // getting GPS status
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

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
                Log.d("Network", "Network Enabled");
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
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitudine = location.getLatitude();
                            longitudine = location.getLongitude();
                        }
                    }
                }
            }
        /*Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String best = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(best, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
        Location location = locationManager.getLastKnownLocation(best);
        Log.d("LOCATION", "location: " + location);

        if (location != null) {
            latitudine = location.getLatitude();
            longitudine = location.getLongitude();
            Log.d("MAIN LATITUDE", "latitude: " + latitudine);
            Log.d("MAIN LONGITUDE", "longitude: " + longitudine);
        } else {
            //TODO
        }*/
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitudine);
            bundle.putDouble("longitude", longitudine);
            fragmentMap.setArguments(bundle);
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
                //mg.setImageBitmap(image);
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // removeAccount(account);
                        notifyAccountDataChanged();
                    }
                });*/
                myAccount.setPhoto(bitmap);
                Log.d("FROM THREAD MAIN BITMAP", bitmap.toString());
                pDialog.dismiss();

            }else{

                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
		           /* String message = String.format(
		                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
		                    location.getLongitude(), location.getLatitude()
		            );
		            Toast.makeText(WarningActivity.this, message, Toast.LENGTH_LONG).show();*/
            latitudine = location.getLatitude();
            longitudine = location.getLongitude();
            Log.d("ON LOCATION CHANGED", "Lat: " + latitudine + " Lon: " + longitudine);
        }

        public void onStatusChanged(String s, int i, Bundle b) {
		            /*Toast.makeText(WarningActivity.this, "Provider status changed",
		                    Toast.LENGTH_LONG).show();*/
        }

        public void onProviderDisabled(String s) {
		            /*Toast.makeText(WarningActivity.this,
		                    "Provider disabled by the user. GPS turned off",
		                    Toast.LENGTH_LONG).show();*/
        }
        public void onProviderEnabled(String s) {
		            /*Toast.makeText(WarningActivity.this,
		                    "Provider enabled by the user. GPS turned on",
		                    Toast.LENGTH_LONG).show();*/
        }
    }
}
