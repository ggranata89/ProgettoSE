package com.example.ricky.mycity;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by giuseppe on 31/05/15.
 */
public class AdapterReport extends ArrayAdapter<Report> {
    private Context context;
    private ArrayList<Report> reportArrayList;

    public AdapterReport(Context ctx,ArrayList<Report> list) {
        super(ctx,R.layout.row_layout ,list);
        this.context = ctx;
        this.reportArrayList = list;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_layout, parent, false);
        }
        // Now we can fill the layout with the right values
        TextView tvTitle = (TextView) convertView.findViewById(R.id.title);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.description);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);
        ImageView imgView = (ImageView) convertView.findViewById(R.id.img);
        Report report = reportArrayList.get(position);

        tvTitle.setText(report.getTitle());
        tvDescription.setText("" + report.getBody());
        tvDate.setText(""+report.getDate());


        if(report.getStatus().equals("Open"))
            imgView.setImageResource(R.drawable.sad);
        else imgView.setImageResource(R.drawable.smile);



        return convertView;
    }
}

