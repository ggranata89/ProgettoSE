package com.example.ricky.mycity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentReport extends Fragment implements Costanti,View.OnClickListener, AdapterView.OnItemSelectedListener {
    private String sessid,session_name,token, name,fid;
    private String title,description,encoded_image;
    private double latitude, longitude;
    private String path = "";
    private int priority_index,category_index;
    private EditText tvTitle,tvDescription;
    private RadioGroup CategoryGroup,PriorityGroup;
    private Uri fileUri;
    private ImageButton cameraButton;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        tvTitle = (EditText) rootView.findViewById(R.id.report_title);
        tvDescription = (EditText) rootView.findViewById(R.id.report_body);
        Spinner prioritySpinner = (Spinner) rootView.findViewById(R.id.priority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.priority_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);
        prioritySpinner.setOnItemSelectedListener(this);
        Spinner categorySpinner = (Spinner) rootView.findViewById(R.id.category);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.category_array, android.R.layout.simple_spinner_dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter1);
        categorySpinner.setSelection(1);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("LISTENER CATEGORY", "CATEGORY: " + i);
                category_index = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //TODO
            }
        });
        SharedPreferences user_details = this.getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        sessid = user_details.getString("sessid","");
        session_name = user_details.getString("session_name", "");
        token = user_details.getString("token", "");

        Bundle args = getArguments();
        latitude = args.getDouble("latitude", 0.0);
        longitude = args.getDouble("longitude", 0.0);

        cameraButton = (ImageButton) rootView.findViewById(R.id.camera);
        cameraButton.setOnClickListener(this);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_send:
                title = tvTitle.getText().toString().trim();
                description = tvDescription.getText().toString().trim();
                new doSendReport().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_report, menu);
    }

    public void sendMyReport(){
        title = tvTitle.getText().toString().trim();
        description = tvDescription.getText().toString().trim();
        new doSendReport().execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        priority_index = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //TODO
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            }
        }

    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCity");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCity Album", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            name = "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    name);
        } else {
            return null;
        }

        return mediaFile;
    }

        /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            encoded_image = createEncodedImage(path);
            new doUploadImage().execute(encoded_image);
        }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(getActivity(), "Image saved to:\n" + this.fileUri, Toast.LENGTH_LONG).show();
                path = fileUri.getPath();
                setBackgroundIconCamera(path);
                encoded_image = createEncodedImage(path);
                new doUploadImage().execute(encoded_image);
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    public void setBackgroundIconCamera(String path){
        cameraButton = (ImageButton) getActivity().findViewById(R.id.camera);
        Drawable d = null;
        if(path != null){
            d = Drawable.createFromPath(path);
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            // Scale it to 50 x 50
            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 96, 96, true));
            //d = Drawable.createFromPath(path);
            //d.setBounds(0, 0, 6, 6);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                cameraButton.setBackgroundDrawable(dr);
            } else {
                cameraButton.setBackground(dr);
            }
        }
    }

    private String createEncodedImage(String path) {

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
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
            StringEntity se;
            try {

                httppost.setHeader("Cookie", session_name + "=" + sessid);
                httppost.setHeader("X-CSRF-Token", token);
                JSONObject dataOut = new JSONObject();
                dataOut.put("file", encoded_image);

                dataOut.put("filename", name);
                dataOut.put("target_uri", "/var/www/html/sites/default/files/"+name);

                se = new StringEntity(dataOut.toString());
                httppost.setEntity(se);
                HttpResponse httpResponse = mHttpClient.execute(httppost);
                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                Log.d("FROM FRAGMENT RESPONSE", "JSONRESPONSE: " + jsonResponse);
                JSONObject jsonObject = new JSONObject(jsonResponse);
                fid = jsonObject.getString("fid");
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
                jsonObject.put("field_report_location",buildLocationJSON(String.valueOf(latitude), String.valueOf(longitude)));
                jsonObject.put("field_img_report", buildImageField(name));

                StringEntity stringEntity = new StringEntity(jsonObject.toString());
                httpPost.setEntity(stringEntity);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String jsonResponse = EntityUtils.toString(httpResponse.getEntity());
                Log.d("DO SEND REPORT", jsonResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private JSONObject buildImageField(String s) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject nestedJSON = new JSONObject();
        try {
            nestedJSON.put("fid",fid);
            nestedJSON.put("uri","http://46.101.148.74/rest/file/"+fid);
            jsonArray.put(nestedJSON);
            json.put("und",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private JSONObject buildDescriptionJSON(String description) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject nestedJSON = new JSONObject();
        try {
            nestedJSON.put("value",description);
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
            nestedJSON.put("geom","POINT("+longitude+" "+latitude+")");
            jsonArray.put(nestedJSON);
            json.put("und",jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
