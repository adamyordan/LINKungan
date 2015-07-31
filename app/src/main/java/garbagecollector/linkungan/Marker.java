package garbagecollector.linkungan;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Jefly on 7/26/2015.
 */
public class Marker implements ClusterItem {
    public String postId;
    public double latitude;
    public double longitude;

    public Marker(String postId, double latitude, double longitude){
        this.postId = postId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(latitude,longitude);
    }
}
