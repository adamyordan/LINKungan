package garbagecollector.linkungan;

/**
 * Created by Jefly on 7/6/2015.
 *
 * Callback ini akan berisi user setelah kita login
 */
public interface GetUserCallback {
    /**
     * returned user merupakan user yang masuk yang dikembalikan dari proses login dari server
     * @param returnedUser
     */
  public abstract void done(User returnedUser);
}
