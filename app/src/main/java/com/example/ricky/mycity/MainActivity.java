package com.example.ricky.mycity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;

public class MainActivity extends MaterialNavigationDrawer implements MaterialAccountListener {

    private String name, mail, img_url;
    private ProgressDialog pDialog;
    private Bitmap bitmap;
    private MaterialAccount myAccount;

    @Override
    public void init(Bundle savedInstanceState){

        Intent intent = getIntent();
        String user = intent.getStringExtra(LoginActivity.USER_DETAILS);
        img_url = intent.getStringExtra(LoginActivity.USER_IMAGE);
        Log.d("FROM MAIN", img_url);

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

        new LoadImage().execute(img_url);

        //Log.d("MAIN BITMAP", bitmap.toString());
        //Account
        myAccount = new MaterialAccount(this.getResources(), name, mail, R.drawable.photo, R.drawable.background);
        this.addAccount(myAccount);

        this.setAccountListener(this);

        //setDrawerHeaderImage(R.drawable.background);
        //setUsername(getString(R.string.app_name));
        //setUserEmail(getAppVersion());
        //this.addSection(newSection(getString(R.string.info), new FragmentIndex()));
        //this.addSection(newSection("Section 2", new FragmentIndex()));
        this.addSection(newSection(getString(R.string.invia_segnalazione), R.mipmap.send_now, new FragmentButton()));
        this.addSection(newSection(getString(R.string.mappa_segnalazione), R.mipmap.map, new FragmentButton()));
        //this.addSubheader("Categoria");
        this.addDivisor();
        this.addSection(newSection(getString(R.string.profile), R.mipmap.profile, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.info), R.mipmap.info, new FragmentButton()).setSectionColor(Color.parseColor("#9c27b0")));
        this.addSection(newSection(getString(R.string.aiuto), R.mipmap.aiuto, new FragmentButton()).setSectionColor(Color.parseColor("#03a9f4")));
        this.addSection(newSection(getString(R.string.credits), R.mipmap.map, new FragmentButton()).setSectionColor(Color.parseColor("#03a9f4")));

        this.addSubheader(getAppVersion());

        this.addBottomSection(newSection(getString(R.string.bottom), R.mipmap.settings, new Intent(this,Settings.class)));
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
}
