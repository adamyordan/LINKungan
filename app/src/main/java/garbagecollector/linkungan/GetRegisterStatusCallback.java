package garbagecollector.linkungan;

/**
 * Created by Jefly on 7/10/2015.
 * Interface yang berisi abstract method yang akan memberikan hasil setelah data di post
 */
public interface GetRegisterStatusCallback {
    /**
     * result berisi indikator apabila proses registrasi atau aktivasi error atau tidak
     * @param result
     */
    public abstract void done(String[] result);
}
