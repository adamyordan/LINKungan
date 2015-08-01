package garbagecollector.linkungan;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jefly on 7/13/2015.
 *
 * Kelas adapter yang mengatur listView (sekumpulan post/feed)
 */
public class PostItemAdapter extends ArrayAdapter<PostItem> {
        //context dan activity dari parent
       private Activity myContext;
       private Context context;
       private ServerRequest serverRequest;
       private UserLocalStore userLocalStore;
       //list of post/feed
       private List<PostItem> listPostItem;

    public PostItemAdapter(Context context, int resource, List<PostItem> objects) {
        super(context, resource, objects);
        userLocalStore = new UserLocalStore(context);
        serverRequest = new ServerRequest(context);
        myContext = (Activity) context;
        this.context = context;
        listPostItem = objects;
    }
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = myContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.post_item, null);

        if(listPostItem != null) {
            //TODO: photo profile, sementara diambil dari gambar di drawable
            PostItem p = listPostItem.get(position);

            switch (p.status){
                case PostItem.REPORTED:
                    rowView = inflater.inflate(R.layout.post_item, null);
                    break;
                case PostItem.MANAGED:
                    rowView = inflater.inflate(R.layout.post_item_2, null);
                    break;
                case PostItem.PROCESSING:
                    break;
                case PostItem.FINISHED:
                    break;
            }

            ImageView thumbImageView = (ImageView) rowView.findViewById(R.id.postThumb);

            if (p.postThumbUrl == null) {
                thumbImageView.setImageResource(R.drawable.profile);
            }

            //set Semua template post menjadi data yang sesuai
            TextView postName = (TextView) rowView.findViewById(R.id.postTitleLabel);
            postName.setText(p.postName);

            TextView postDateView = (TextView) rowView.findViewById(R.id.postDateLabel);
            postDateView.setText(p.postDate);

            final TextView postDescription = (TextView) rowView.findViewById(R.id.postDescription);
            postDescription.setText(p.postDescription);

            ImageView postImage = (ImageView) rowView.findViewById(R.id.postImage);
            //Picasso library dari http://square.github.io/picasso/
            Picasso.with(context).load("http://garbageserver.esy.es/"+p.postImageUrlSmall).resize(400,200).centerCrop().into(postImage);

            TextView tvAddress = (TextView) rowView.findViewById(R.id.address);
            tvAddress.setText(p.address);

            final TextView tvLike = (TextView)rowView.findViewById(R.id.tvLike);
            tvLike.setText(""+p.totalLike);

            final ImageButton btnLike = (ImageButton) rowView.findViewById(R.id.like);

            if(listPostItem.get(position).isLike){
                btnLike.setImageResource(R.drawable.ic_favorite_black_36dp);
            } else {
                btnLike.setImageResource(R.drawable.ic_favorite_border_black_36dp);
            }
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PostItem post = listPostItem.get(position);

                    serverRequest.likeInBackground(post.isLike, userLocalStore.getLoggedInUser().id, post.id, new GetLikeCallback() {
                        @Override
                        public void done(String message) {
                            if(message == null){
                                Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show();
                            } else {
                                if(message.equals("0")){
                                    if(post.isLike){
                                        btnLike.setImageResource(R.drawable.ic_favorite_border_black_36dp);
                                        post.totalLike--;
                                        //tvLike.setText(post.totalLike);
                                    } else {
                                        btnLike.setImageResource(R.drawable.ic_favorite_black_36dp);
                                        post.totalLike++;
                                        //tvLike.setText(post.totalLike);
                                    }
                                    post.isLike = !post.isLike;
                                    listPostItem.set(position, post);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context, "Internal error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } );
                }
            });
        }
        return rowView;
    }
}
