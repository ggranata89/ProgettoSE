package com.example.ricky.mycity;

/**
 * Created by ricky on 5/4/15.
 */
public interface Costanti {
    public static String LOGIN_URI = "http://46.101.148.74/rest/user/login";
    public static String REGISTER_URI = "http://46.101.148.74/rest/user/register";
    public static String REPORT_URI = "http://46.101.148.74/rest/views/reports_list";
    public static String LOGOUT_URI = "http://46.101.148.74/rest/user/logout";
    public static String NODE_URI = "http://46.101.148.74/rest/node/";
    public static String FILE_URI = "http://46.101.148.74/rest/file/";
    public static String TOKEN_URI = "http://46.101.148.74/services/session/token";
    public static String TARGET_URI = "/var/www/html/sites/default/files/";

    //Costanti utilizzate dalla camera
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    //public static String MY_REPORTS_URI = "http://46.101.148.74/rest/views/myreportslist";

    // Costanti relative alla localizzazione utente
    public static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in metri
    public static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in millisecondi
}
