package com.example.ricky.mycity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by giuseppe on 15/05/15.
 */
public class FragmentReport extends FragmentButton implements Costanti,View.OnClickListener {
    private String sessid,session_name,token;
    private String title,description,encoded_image;
    int priority_index,category_index;
    int lid = 40;
    EditText tvTitle,tvDescription;
    RadioGroup CategoryGroup,PriorityGroup;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        tvTitle = (EditText) rootView.findViewById(R.id.report_title);
        tvDescription = (EditText) rootView.findViewById(R.id.report_body);
        PriorityGroup = (RadioGroup) rootView.findViewById(R.id.Priority_RadioGroup);
        CategoryGroup = (RadioGroup) rootView.findViewById(R.id.Category_RadioGroup);

        sessid = getActivity().getIntent().getStringExtra("sessid");
        session_name = getActivity().getIntent().getStringExtra("session_name");
        token = getActivity().getIntent().getStringExtra("token");

        Button b = (Button) rootView.findViewById(R.id.send_report);
        b.setOnClickListener(this);

        Button cameraButton = (Button) rootView.findViewById(R.id.camera);
        cameraButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        String path = "";
        switch (v.getId()){
            case R.id.send_report:
                title = tvTitle.getText().toString().trim();
                description = tvDescription.getText().toString().trim();
                Log.v("REPORT-TITLE",title);
                Log.v("REPORT-DESCRIPTION",description);
                int radioButtonID = PriorityGroup.getCheckedRadioButtonId();
                View radioButton = PriorityGroup.findViewById(radioButtonID);
                priority_index = PriorityGroup.indexOfChild(radioButton);
                Log.v("REPORT-Priority", String.valueOf(priority_index));

                radioButtonID = CategoryGroup.getCheckedRadioButtonId();
                radioButton = CategoryGroup.findViewById(radioButtonID);
                category_index = CategoryGroup.indexOfChild(radioButton);
                Log.v("REPORT-Category", String.valueOf(category_index));
                Log.v("Photo",path);
                new doSendReport().execute();
                break;

            case R.id.camera:
                Log.v("REPORT","Image button pressed");
                File destination = new File(Environment
                        .getExternalStorageDirectory(), "report_photo.jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
                path = Uri.fromFile(destination).getPath();
                Log.v("Photo2",path);
                startActivityForResult(intent, 2);
                encoded_image = createEncodedImage(path);
                new doUploadImage().execute(encoded_image);
                break;
        }


    }

    private String createEncodedImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArray);
        byte[] byteArrayImage = byteArray.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage,Base64.DEFAULT);
        return encodedImage;
    }

    private class doUploadImage extends AsyncTask<String,Void,Void> {

        @Override
        protected Void doInBackground(String... encodedImage) {
            HttpClient mHttpClient = new DefaultHttpClient();
            HttpParams mHttpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
            HttpConnectionParams.setSoTimeout(mHttpParams, 10000);


            HttpPost httppost = new HttpPost(FILE_URI);
            httppost.setHeader("Content-type", "application/json");

            String cookie;
            StringEntity se;
            try {

                httppost.setHeader("Cookie", session_name + "=" + sessid);
                httppost.setHeader("X-CSRF-Token", token);
                JSONObject dataOut = new JSONObject();
                //dataOut.put("file", encodedImage);
                dataOut.put("file", "/storage/emulated/0/report_photo.jpg");
                Log.d("ENCODED_IMAGE",encoded_image);
                se = new StringEntity(dataOut.toString());
                httppost.setEntity(se);
                HttpResponse response = mHttpClient.execute(httppost);
                String logResponse = EntityUtils.toString(response.getEntity());
                Log.d("FILE_RESPONSE", "re:" + logResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


    }

    private class doSendReport extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(NODE_URI);

                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Cookie", session_name + "=" + sessid);
                httpPost.setHeader("X-CSRF-Token", token);


                JSONObject jsonObject = new JSONObject();

                jsonObject.put("title", title);
                jsonObject.put("type", "report");
                jsonObject.put("body", buildDescriptionJSON(description));

                jsonObject.put("field_priority", buildJSON(String.valueOf(priority_index + 1)));
                jsonObject.put("field_category",buildJSON(String.valueOf(category_index + 1)));



                jsonObject.put("field_report_location",buildLocationJSON("37.553900", "15.010071"));


                Log.v("JSON-BUILD", jsonObject.toString());

                StringEntity stringEntity = new StringEntity(jsonObject.toString());

                httpPost.setEntity(stringEntity);


                HttpResponse httpResponse = httpClient.execute(httpPost);

                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                Log.v("REPORT-SEND", jsonResponse);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private JSONObject buildDescriptionJSON(String description) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject nestedJSON = new JSONObject();
        try {
            nestedJSON.put("value",description);
            //nestedJSON.put("summary","");
            //nestedJSON.put("format","filtered_html");
            //nestedJSON.put("safe_value","<p>"+description+"</p>");
            //nestedJSON.put("safe_summary","");
            jsonArray.put(nestedJSON);
            json.put("und",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


    private JSONObject buildJSON(String value) {
        JSONObject json = new JSONObject();
        JSONObject nestedJSON = new JSONObject();
        try {
            nestedJSON.put("value",value);

            json.put("und",nestedJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private JSONObject buildLocationJSON(String latitude,String longitude) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject nestedJSON = new JSONObject();

        try {
            nestedJSON.put("input_format","GEOFIELD_INPUT_WKT");
            nestedJSON.put("geom","POINT("+latitude+" "+longitude+")");
            jsonArray.put(nestedJSON);
            json.put("und",jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }


}
