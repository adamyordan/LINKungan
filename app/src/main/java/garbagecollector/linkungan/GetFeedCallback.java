package garbagecollector.linkungan;


import java.util.List;

/**
 * Created by Jefly on 7/13/2015.
 *
 * Callback yang berisi abstract method yang nanti diimplementasikan setelah proses menerima Feed/Post data selesai
 */
public interface GetFeedCallback {
    /**
     * List berisikan timeline post di dalam home tab
     * @param posts
     */
    public abstract void done(List<PostItem> posts);
}
