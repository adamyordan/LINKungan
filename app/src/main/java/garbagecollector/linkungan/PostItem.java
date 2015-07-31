package garbagecollector.linkungan;

import android.graphics.Bitmap;

/**
 * Created by Jefly on 7/13/2015.
 *
 * Kelas yang merepresentasikan sebuah feed/post
 */
public class PostItem {
    //id post
    public String id;
    //link photo profile
    public String postThumbUrl;
    //Nama user yang posting
    public String postName;
    //Tanggal posting
    public String postDate;
    //Url image yg same quality
    public String postImageUrl;
    //Url image yg low quality (thumbnail)
    public String postImageUrlSmall;
    //Deskripsi
    public String postDescription;
    public int totalLike;
    public boolean isLike;
    public String address;
    public double latitude;
    public double longitude;
    //TODO:jumlah like, status post(udah dibikin event/blom)

    public PostItem(String id, String postThumbUrl,  String postName, String postDate, String postImageUrl, String postImageUrlSmall, String postDescription, int totalLike, boolean isLike, String address, double latitude, double longitude){
        this.id = id;
        this.postThumbUrl = postThumbUrl;
        this.postName = postName;
        this.postDate = postDate;
        this.postImageUrl = postImageUrl;
        this.postImageUrlSmall = postImageUrlSmall;
        this.postDescription = postDescription;
        this.totalLike = totalLike;
        this.isLike = isLike;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
