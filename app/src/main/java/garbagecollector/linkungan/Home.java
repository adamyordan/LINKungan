package garbagecollector.linkungan;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;

/**
 * Main Activity aplikasi
 *
 */

//TODO: tambahin tab event dkk

public class Home extends ActionBarActivity implements View.OnClickListener {
    //sharedPreferences dari aplikasi
    private UserLocalStore userLocalStore;
    //pager / tab-tab
    private ViewPager pager;
    //adapter yang mengatur pager
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private CharSequence Titles[]={"Reports","Nearby","You"};
    private int Numboftabs =3;
    private boolean firstBackPressed;

    //================Upload variable====================//

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    private Uri fileUri; // file url to store image
    public FloatingActionButton btnCapturePicture;

    // ProfileFragment
    private Button btnLogout;
    private Button btnChangePassword;
    private ServerRequest serverRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("login", "create Home");

        //Setting warna dan title di action bar
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>LINKungan</font>"));


        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        userLocalStore = new UserLocalStore(this);

        //button di kiri bawah layout


        //ProfileFragment
        serverRequest = new ServerRequest(this);
    }

    public View onCreateView(String name, Context context, AttributeSet attr){
        View v = super.onCreateView(name, context, attr);
        return v;

    }

    /**
     * apabila button capture ditekan maka capture image
     * @param v
     */
    @Override
    public void onClick(View v) {
        if(isConnectingToInternet()) {
            // capture picture
            captureImage();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("We need to get your location, Please check your internet connection");
            dialog.setPositiveButton("Ok", null);
            dialog.show();
        }
    }

    /**
     * Apabila tekan back
     */
    @Override
    public void onBackPressed() {
        if(firstBackPressed){
            this.finish();
            System.exit(0);
        } else {
            firstBackPressed = true;
            Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firstBackPressed = false;
                }
            }, 3000);
        }
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
            startActivity(new Intent(Home.this, Login.class));
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //check apabila keadaan login
        if(!new UserLocalStore(this).isLoggedIn()){
            //kembali ke login activity
            startActivity(new Intent(Home.this, Login.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    /**
     * Button listener di action bar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_notification:
                Log.d("action bar", "notif");
                //TODO: notification
                return true;
            case R.id.action_addFriend:
                Log.d("action bar", "addfriend");
                //TODO: add friend
                return true;
            case R.id.action_search:
                Log.d("action bar", "search");
                //TODO: search
                return true;
            case R.id.action_settings:
                Log.d("action bar", "setting");
                //start activity setting
                Intent settingIntent = new Intent(this,Setting.class);
                settingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(settingIntent,0);
                overridePendingTransition(0,0);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    /**
     * Upload method
     *
     * ====================================================================
     * Yang mengatur behaviour tombol capture
     *
     */



    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     *
     * Kayak save state, kalo ganti acticity
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
        outState.putParcelable("viewPagerAdapter", adapter.saveState());
    }

    /**
     * Load state apabila kembali ke activity ini
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
            // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
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
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image

                //get Location
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
                        // launching upload activity
                        launchUploadActivity(address, latLng);
                    }
                }


            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    /**
     * Jalankan activity uploadHelper untuk preview dan upload
     */
    private void launchUploadActivity(String address, LatLng latLng){
        Intent i = new Intent(Home.this, UploadHelper.class);
        //kirim data filepath ke activity uploadHelper
        i.putExtra("filePath", fileUri.getPath());
        //kirim location ke activity uploadHelper
        i.putExtra("address", address);
        //kirim latitude ke activity uploadHelper
        i.putExtra("latitude", latLng.latitude);
        //kirim longitude ke activity uploadHelper
        i.putExtra("longitude", latLng.longitude);
        startActivity(i);
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

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

    public void togglesContent(View v){
        ViewGroup toggledContent = (ViewGroup) v.findViewById(R.id.toggled_content);
        toggledContent.setVisibility(toggledContent.isShown() ? View.GONE : View.VISIBLE);
    }


}
