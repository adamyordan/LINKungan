package garbagecollector.linkungan;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Visibility;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab Home Fragment
 */
public class Tab1 extends Fragment {
    //List posts/feeds
    private List<PostItem> listPostItem;
    //Layout agar refresh new atau old post jika diswipe
    private SwipyRefreshLayout swipeContainer;
    //listView, semacem <ul> kalo di HTML
    private ListView listView;
    //server request
    private ServerRequest serverRequest;
    //adapter yang update dan refresh listView
    private PostItemAdapter itemAdapter;
    //circular progress bar pada saat loading
    private LinearLayout headerProgressLayout;
    private ProgressBar progressBar;

    //id post terbaru
    private int newId;
    //id post terlama
    private int oldId;

    //request Code apabila meminta post terbaru
    private final int REQUEST_NEW = 1;

    //request Code apabila meminta post lama
    private final int REQUEST_OLD = 0;

    //untuk menghindari refresh sekaligus loading post
    private boolean isLoading;

    private UserLocalStore userLocalStore;

    FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);
        headerProgressLayout = (LinearLayout) rootView.findViewById(R.id.headerProgressLayout);
        userLocalStore = new UserLocalStore(getActivity());
        swipeContainer = (SwipyRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(!isLoading) {
                    //jika mentok di atas, minta update post terbaru
                    if (direction == SwipyRefreshLayoutDirection.TOP) {
                        Log.d("update", "top");
                        getUpdateFeeds(10, newId, REQUEST_NEW);
                        //jika mentok dibawah, minta update post yang lama
                    } else if (direction == SwipyRefreshLayoutDirection.BOTTOM) {
                        getUpdateFeeds(10, oldId, REQUEST_OLD);
                        Log.d("update", "bottom");
                    }
                }
            }
        });
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        listView = (ListView) rootView.findViewById(R.id.postListView);
        serverRequest = new ServerRequest(getActivity());
        listPostItem = new ArrayList<>();
        isLoading = true;
        getFeeds();
        itemAdapter = new PostItemAdapter(this.getActivity(),
                R.layout.post_item, listPostItem);
        Log.d("tab", "generate complete");
        listView.setAdapter(itemAdapter);

        fab = (FloatingActionButton) rootView.findViewById(R.id.btnCapture);
        fab.attachToListView(listView);
        fab.show();
        fab.setOnClickListener((Home) getActivity());
        ((Home) getActivity()).btnCapturePicture = fab;


        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Get posts pada saat fragment pertama kali dibuat
     */
    private void getFeeds() {
        headerProgressLayout.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        serverRequest.getFeedInBackGround(10 ,userLocalStore.getLoggedInUser().id,new GetFeedCallback() {
            @Override
            public void done(List<PostItem> posts) {
                progressBar.setIndeterminate(false);
                headerProgressLayout.setVisibility(View.GONE);
                listPostItem = posts;
                //jika koneksi berhasil
                if(posts != null) {
                    //jika post ada, update newId dan oldId
                    if (posts.size() != 0) {
                        newId = Integer.parseInt(posts.get(0).id);
                        oldId = Integer.parseInt(posts.get(posts.size() - 1).id);
                        Log.d("feed", "new = " + newId + ", old = " + oldId);
                        //add post ke itemAdapter, otomatis akan ditambahkan ke listView post-nya
                        for (int i = 0; i < posts.size(); i++) {
                            itemAdapter.add(posts.get(i));
                        }
                        itemAdapter.notifyDataSetChanged();
                        Log.d("feed", listPostItem.toString());
                    }
                    //jika koneksi bermasalah
                } else {
                   showErrorDialog();
                }
                isLoading = false;
            }
        });
    }

    private void getUpdateFeeds(int numFeed, int flagId, final int requestCode){
        serverRequest.getUpdateFeedInBackGround(numFeed, flagId, requestCode, userLocalStore.getLoggedInUser().id, new GetFeedCallback() {
            @Override
            public void done(List<PostItem> posts) {
                List<PostItem> tmp = posts;
                if (tmp != null) {
                    if (requestCode == REQUEST_OLD) {
                        int index = listView.getFirstVisiblePosition();
                        if (tmp.size() > 0) {
                            oldId = Integer.parseInt(tmp.get(tmp.size() - 1).id);
                            Log.d("feed", tmp.get(tmp.size() - 1).id + "");
                            for (int i = 0; i < tmp.size(); i++) {
                                itemAdapter.add(tmp.get(i));
                            }
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            showRefreshDialog("There's no more post");
                        }
                        listView.setSelection(index);
                    } else {
                        for (int i = tmp.size() - 1; i >= 0; i--) {
                            itemAdapter.insert(tmp.get(i), 0);
                        }
                        if (tmp.size() > 0) {
                            newId = Integer.parseInt(tmp.get(0).id);
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            showRefreshDialog("already up-to-date");
                        }
                    }
                } else {
                    showErrorDialog();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    private void showErrorDialog(){
        Toast.makeText(getActivity(), "Error while getting post", Toast.LENGTH_SHORT).show();
    }

    private void showRefreshDialog(String message){
        Toast toast= Toast.makeText(getActivity(),
                message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

}
