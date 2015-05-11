package com.example.ricky.mycity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

/**
 * Created by ricky on 2/20/15.
 */
public class FragmentMap extends Fragment {

    private GoogleMap googleMap;
    private MapView mapView;

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

        googleMap = mapView.getMap();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.75, 15.00), 10));
        googleMap.setMyLocationEnabled(true);
        return rootView;
    }
}
