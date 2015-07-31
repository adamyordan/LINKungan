package garbagecollector.linkungan
        ;
import garbagecollector.linkungan.AndroidMultiPartEntity.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.maps.model.LatLng;

public class UploadHelper extends ActionBarActivity implements View.OnClickListener {


    private ProgressBar progressBar;
    private String filePath = null;
    private String etDescriptionText;
    private ImageView imgPreview;
    private ImageButton btnRecapturePicture;
    private EditText etDescription;
    private TextView tvAddress;
    private Uri fileUri;
    private ProgressDialog progressDialog;
    private UserLocalStore userLocalStore;
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private long totalSize = 0;
    private String address;
    private LatLng latLng;
    private double latitude;
    private double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_helper);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>Post</font>"));
        userLocalStore = new UserLocalStore(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait...");
        etDescriptionText = "";
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        etDescription = (EditText) findViewById(R.id.etDescription);
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        etDescription.setHint("Describe your picture...");
        etDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                etDescription.setHint("");
            }
        });
        btnRecapturePicture = (ImageButton) findViewById(R.id.btnRecapture);
        btnRecapturePicture.setOnClickListener(this);

        // Receiving the data from previous activity
        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");
        address = i.getStringExtra("address");
        //maksimum latitude di bumi = 85 , maksimum longitude di bumi = 180
        latitude = i.getDoubleExtra("latitude", 300);
        longitude = i.getDoubleExtra("longitude", 300);

        if(address != null || latitude == 300 ||longitude == 300){
            tvAddress.setText(address);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Error while getting location", Toast.LENGTH_LONG).show();
        }

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        recaptureImage();
    }
    /**
     * Launching camera app to capture image
     */
    private void recaptureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                UploadConfig.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("result", "Oops! Failed create "
                        + UploadConfig.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");


        return mediaFile;
    }
    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // successfully captured the image
                // launching upload activity
                filePath = fileUri.getPath();
                LatLng latLng = getCurrentLocation(this);
                if(latLng == null){
                    Toast.makeText(getApplicationContext(),
                            "Error while getting your location", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    String address = getAddress(latLng.latitude, latLng.longitude);
                    if(address == null){
                        Toast.makeText(getApplicationContext(),
                                "Error while getting your location", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        tvAddress.setText(address);
                        this.latitude = latLng.latitude;
                        this.longitude = latLng.longitude;
                        previewMedia();
                    }
                }


            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public LatLng getCurrentLocation(Context context)
    {
        try
        {
            LocationManager locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String locProvider = locMgr.getBestProvider(criteria, false);
            Location location = locMgr.getLastKnownLocation(locProvider);

            // getting GPS status
            boolean isGPSEnabled = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            boolean isNWEnabled = locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNWEnabled)
            {
                // no network provider is enabled
                return null;
            }
            else
            {
                // First get location from Network Provider
                if (isNWEnabled)
                    if (locMgr != null)
                        location = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled)
                    if (location == null)
                        if (locMgr != null)
                            location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (NullPointerException ne)
        {
            Log.e("Current Location", "Current Lat Lng is Null");
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        if(isConnectingToInternet()) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    for(int i = 0; i<=address.getMaxAddressLineIndex();i++){
                        if(i!= address.getMaxAddressLineIndex())
                            result.append(address.getAddressLine(i)+", ");
                        else
                            result.append(address.getAddressLine(i));
                    }
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
        } else {
            return null;
        }
        return result.toString();
    }

    /**
     * Method yang akan dijalankan pada saat activity ini dibuat
     */
    @Override
    protected void onStart(){
        super.onStart();
        //check apabila keadaan login
        if(!new UserLocalStore(this).isLoggedIn()){
            //kembali ke login activity
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //check apabila keadaan login
        if(!new UserLocalStore(this).isLoggedIn()){
            //kembali ke login activity
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload_helper, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        new UploadFileToServer(this).execute();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia() {
        // Checking whether captured media is image or video

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);

    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
        outState.putString("et", etDescription.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
        etDescriptionText = savedInstanceState.getString("et");
        etDescription.setText(etDescriptionText);
    }



    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        Context context;
        UploadFileToServer(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressDialog.show();
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);


        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(UploadConfig.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);
                User user = userLocalStore.getLoggedInUser();
                // Adding file data to http body
                Log.d("upload", address);

                entity.addPart("image", new FileBody(sourceFile));
                entity.addPart("id", new StringBody(user.id));
                entity.addPart("description", new StringBody(etDescription.getText().toString()));
                entity.addPart("address", new StringBody(address));
                entity.addPart("latitude", new StringBody(""+latitude));
                entity.addPart("longitude", new StringBody(""+longitude));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = "Image Uploaded";
                } else {
                    responseString = "error";
                }

            } catch (ClientProtocolException e) {
                responseString = "error";
            } catch (IOException e) {
                responseString = "error";
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("result", "Response from server: " + result);

            // showing the server response in an alert dialog

            progressDialog.dismiss();
            if(result.equalsIgnoreCase("error")) {
                showAlert("Error occurred while uploading");
                progressBar.setProgress(0);
                progressBar.setVisibility(View.GONE);
            } else {
                showAlert(result);
                Intent intent = new Intent(context, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0);
                finish();
            }
            super.onPostExecute(result);

        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
      Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_LONG).show();

    }

}