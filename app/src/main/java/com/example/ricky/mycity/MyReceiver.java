package com.example.ricky.mycity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ON RECEIVE", "INTENT RECEIVED");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(null==networkInfo)/*{
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return 1;
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return 2;
        }else*/{
            Toast.makeText(context, "Connessione assente", Toast.LENGTH_LONG).show();
        }
    }
}
