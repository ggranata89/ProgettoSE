package com.example.ricky.mycity;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by giuseppe on 07/05/15.
 */
public class Report {
    private String title;
    private String body;
    private LatLng location;
    private String priority;
    private String category;
    private String user;
    private String date;

    public Report(String title,String body, LatLng location, String priority,String category,String user,String date){
        this.title = title;
        this.body = body;
        this.location = location;
        this.priority = priority;
        this.category = category;
        this.user = user;
        this.date = date;
    }

    public String getTitle(){ return title;}

    public String getBody(){
        return body;
    }

    public LatLng getLocation(){
        return location;
    }

    public String getPriority(){
        return priority;
    }

    public String getCategory(){
        return category;
    }

    public String getUser(){ return user;}

    public void setTitle(String title){ title = this.title;}

    public void setBody(String description){
        body = description;
    }

    public void setLocation(LatLng location){
        this.location = location;
    }

    public void setPriority(String priority){
        this.priority = priority;
    }

    public void setUser(String user){this.user = user;}

    public String getDate(){
        return date;
    }

}
