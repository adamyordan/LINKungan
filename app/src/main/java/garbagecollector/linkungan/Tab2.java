package garbagecollector.linkungan;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//TODO:Event
public class Tab2 extends Fragment {
    private GoogleMap map;
    //private SupportMapFragment fragment;
    MapView mapView;
    ServerRequest serverRequest;
    ClusterManager clusterManager;
    ProgressBar refreshProgressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)          {
        View v = inflater.inflate(R.layout.fragment_tab2, container, false);

        Toast.makeText(getActivity(), "anjrid", Toast.LENGTH_SHORT).show();

        serverRequest = new ServerRequest(getActivity());

        if(isGoogleMapsInstalled()){
            setUpMap(v, savedInstanceState);
        } else {
            Toast.makeText(getActivity(), "Google maps / Google services May not installed", Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    public boolean isGoogleMapsInstalled()
    {
        try
        {
            ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    public void setUpMap(View v , Bundle savedInstanceState){
        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);

        clusterManager = new ClusterManager<garbagecollector.linkungan.Marker>(getActivity(), map);
        clusterManager.setRenderer(new MyClusterRenderer(getActivity(), map, clusterManager));


        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());
        LatLng currentLatLng = getCurrentLocation(getActivity());
        CircleOptions circleOptions = new CircleOptions().center(currentLatLng).radius(10000).fillColor(0x22008A00).strokeColor(0xff008A00).strokeWidth(2);
//        final Circle mCircle = map.addCircle(circleOptions);

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
//                mCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 11);
//        map.animateCamera(cameraUpdate);
        refreshProgressBar = (ProgressBar) v.findViewById(R.id.refreshProgressBar);
        addMarker();

        ImageButton btnRefresh = (ImageButton) v.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshProgressBar.setVisibility(View.VISIBLE);
                refreshProgressBar.setIndeterminate(true);
                clusterManager.clearItems();
                addMarker();

            }
        });
    }

    public void addMarker(){
        serverRequest.getMarkerInBackground(new GetMarkerCallback() {
            @Override
            public void done(ArrayList<garbagecollector.linkungan.Marker> result) {
                refreshProgressBar.setIndeterminate(false);
                refreshProgressBar.setVisibility(View.GONE);
                BitmapDescriptor bitmapDescriptor
                        = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN);
                if(result != null) {
                    for(int i = 0; i<result.size();i++) {
                        garbagecollector.linkungan.Marker marker = result.get(i);
                        clusterManager.addItem(marker);
                    }
                }
                clusterManager.cluster();
            }
        });
    }


    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();

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
            return new LatLng(0, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new LatLng(0, 0);
        }
    }

    public String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        if(isConnectingToInternet()) {
            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public class MyClusterRenderer extends DefaultClusterRenderer<garbagecollector.linkungan.Marker> {

        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());

        public MyClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<garbagecollector.linkungan.Marker> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(garbagecollector.linkungan.Marker item,
                                                   MarkerOptions markerOptions) {

            BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

            markerOptions.icon(markerDescriptor);
        }

        @Override
        protected void onClusterItemRendered(garbagecollector.linkungan.Marker clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }


    }

    /*@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = fragment.getMap();
            map.addMarker(new MarkerOptions().position(new LatLng(1000, 100)));
        }
    }*/
}

