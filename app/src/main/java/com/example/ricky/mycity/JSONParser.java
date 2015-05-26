package com.example.ricky.mycity;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by giuseppe on 26/05/15.
 */
public class JSONParser {

    private JSONObject jsonObject;
    private String title,body,priority,category,user,image,date,uid,status;
    private double latitude;
    private double longitude;
    private Long timestamp;

    public JSONParser(JSONObject json){
        this.jsonObject = json;
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

    public Report getReportFromJSON(){
        try {
            title = jsonObject.getString("title");
            body = jsonObject.getJSONObject("body").getJSONArray("und").getJSONObject(0).getString("value");
            timestamp = Long.parseLong(jsonObject.getString("created"));
            date = convertTimestampToDate(timestamp);

            priority = jsonObject.getJSONObject("field_priority").getJSONArray("und").getJSONObject(0).getString("value");
            priority = getPriorityByIndex(priority);

            category = jsonObject.getJSONObject("field_priority").getJSONArray("und").getJSONObject(0).getString("value");
            category = getCategoryByIndex(category);
            
            status = jsonObject.getJSONObject("field_status").getJSONArray("und").getJSONObject(0).getString("value");


            JSONObject jsonLocation = jsonObject.getJSONObject("field_report_location");
            latitude = Double.parseDouble(jsonLocation.getJSONArray("und").getJSONObject(0).getString("lat"));
            longitude = Double.parseDouble(jsonLocation.getJSONArray("und").getJSONObject(0).getString("lon"));
            user = jsonObject.getString("name");
            uid = jsonObject.getString("uid");
            image = jsonObject.getJSONObject("field_img_report").getJSONArray("und").getJSONObject(0).getString("filename");
        }catch(Exception e){
            e.printStackTrace();
        }
        return new Report(title, body, new LatLng(latitude, longitude), priority, category, user,uid, date, image,status);
    }


}
