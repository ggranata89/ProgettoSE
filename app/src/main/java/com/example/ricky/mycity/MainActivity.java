package com.example.ricky.mycity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;

public class MainActivity extends MaterialNavigationDrawer implements MaterialAccountListener {
    @Override
    public void init(Bundle savedInstanceState){

        //Account
        MaterialAccount myAccount = new MaterialAccount(this.getResources(), "Demo", "demo@example.com", R.drawable.photo, R.drawable.background);
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
        return "\u00A9  Tefranet versione " + packageInfo.versionName;
    }

    @Override
    public void onAccountOpening(MaterialAccount account){
        //TODO
    }

    @Override
    public void onChangeAccount(MaterialAccount account){
        //TODO
    }
}
