package garbagecollector.linkungan;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jefly on 7/6/2015.
 * Kelas yg berhubungan ke server
 */

public class ServerRequest {
    //progress dialog apabila request ke server
    private ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 15000;
    public static final String SERVER_ADDRESS = "http://garbageserver.esy.es/";

    public ServerRequest(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        Log.d("login", "server request class created");
    }

    //fetch data pada saat login
    public void fetchUserDataInBackground(User user, GetUserCallback callback){
        progressDialog.show();
        new FetchUserDataAsyncTask(user, callback).execute();
    }

    //fetch data login facebook
    public void fetchUserFacebookDataInBackground(String facebookId, GetUserCallback callback){
        new FetchUserFacebookDataAsyncTask(facebookId, callback).execute();
    }

    //store register user data
    public void StoreUserDataInBackground(User user, GetRegisterStatusCallback callback){
       new StoreUserDataAsyncTask(user, callback).execute();
    }

    //actiasi user
    public void activeUserInBackGround(User user, String activationCode, GetRegisterStatusCallback callback){
        new ActiveUserAsyncTask(user,activationCode,callback).execute();
    }

    //get post/feed
    public void getFeedInBackGround(int numFeed,String userId ,GetFeedCallback callback){
        new GetFeedAsyncTask(numFeed,userId,callback).execute();
    }

    //get update when refresh
    public void getUpdateFeedInBackGround(int numFeed, int flagId, int requestCode,String userId ,GetFeedCallback callback){
        new GetUpdateFeedAsyncTask(numFeed,flagId,requestCode,userId,callback).execute();
    }

    //ubah password
    public void changePasswordInBackground(String id, String oldPassword, String newPassword, GetRegisterStatusCallback callback ){
        new ChangePasswordAsyncTask(id, oldPassword, newPassword, callback).execute();
    }
    //like post
    public void likeInBackground(boolean isLike, String userId, String postId, GetLikeCallback callback){
        new LikeAsyncTask(isLike, userId, postId, callback).execute();
    }

    public void storeFacebookUserDataAsyncTask(String firstName, String lastName, String facebookId, GetRegisterStatusCallback callback){
        new StoreFacebookUserDataAsyncTask(firstName, lastName, facebookId, callback).execute();
    }

    public void getMarkerInBackground(GetMarkerCallback callback){
        new GetMarkerAsyncTask(callback).execute();
    }

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User>{
        User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User user, GetUserCallback userCallback){
            Log.d("login","async task");
            this.user = user;
            this.userCallback = userCallback;
        }

        /**
         * Do in background, mirip  multi Threading
         * @param params
         * @return
         */
        @Override
        protected User doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            /**
             * akan dikirim di php, karena ini menggunakan post,
             * di php nanti akan jadi $_POST['email'] dan $_POST['password']
             */
            inputData.add(new BasicNameValuePair("email", user.email));
            inputData.add(new BasicNameValuePair("password", user.password));
            Log.d("login", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            //set connection timeout
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"FetchUserData.php");
            Log.d("login", "connected");
            User returnedUser= null;
            try{
                //post data
                post.setEntity(new UrlEncodedFormEntity(inputData));
                /**
                 * meminta response dari server, response yang di 'echo' dari php dengan hasil encoding JSON
                 */
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("login", strEntity);
                //decoding JSON
                JSONObject j = new JSONObject(strEntity);
                Log.d("login", "JSON received");
                String message = j.getString("message");
                if(message.equals("-1")){
                    Log.d("login", "user not found");
                    return null;
                } else {
                    //mengambil output dari response
                    String id = j.getString("id");
                    String firstName = j.getString("firstName");
                    String lastName = j.getString("lastName");
                    String email = j.getString("email");
                    String status = j.getString("status");
                    String password = j.getString("password");
                    returnedUser = new User(id, firstName, lastName, email,password, status);

                }

            } catch(Exception e){
                //exception terjadi apabila connection error
                //atau null pointer
                e.printStackTrace();
                return new User("-1", null, null, null, null, null);
            }
            Log.d("login", "returned user");
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            //Setelah background procces selesai, panggil callback
            Log.d("login","post execute");
            progressDialog.dismiss();
            userCallback.done(returnedUser);
        }
    }

    public class FetchUserFacebookDataAsyncTask extends AsyncTask<Void, Void, User>{
        String facebookId;
        GetUserCallback callback;

        public FetchUserFacebookDataAsyncTask(String facebookId, GetUserCallback callback){
            Log.d("login","async task");
            this.facebookId = facebookId;
            this.callback = callback;
        }

        /**
         * Do in background, mirip  multi Threading
         * @param params
         * @return
         */
        @Override
        protected User doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            /**
             * akan dikirim di php, karena ini menggunakan post,
             * di php nanti akan jadi $_POST['email'] dan $_POST['password']
             */
            inputData.add(new BasicNameValuePair("facebookId", facebookId));
            Log.d("login", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            //set connection timeout
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"FetchUserData.php");
            Log.d("login", "connected");
            User returnedUser= null;
            try{
                //post data
                post.setEntity(new UrlEncodedFormEntity(inputData));
                /**
                 * meminta response dari server, response yang di 'echo' dari php dengan hasil encoding JSON
                 */
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("login", strEntity);
                //decoding JSON
                JSONObject j = new JSONObject(strEntity);
                Log.d("login", "JSON received");
                String message = j.getString("message");
                if(message.equals("1")){
                    return null;
                } else if(message.equals("0")) {
                    //mengambil output dari response
                    String id = j.getString("id");
                    String firstName = j.getString("firstName");
                    String lastName = j.getString("lastName");
                    String email = j.getString("email");
                    String status = "0";
                    String password = j.getString("password");
                    returnedUser = new User(id, firstName, lastName, email,password, status);
                } else {
                    throw new Exception();
                }

            } catch(Exception e){
                //exception terjadi apabila connection error
                //atau null pointer
                e.printStackTrace();
                return new User("-1", null, null, null, null, null);
            }
            Log.d("login", "returned user");
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            //Setelah background procces selesai, panggil callback
            Log.d("login","post execute");
            callback.done(returnedUser);
        }
    }

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, String[]>{
        String[] result;
        User user;
        GetRegisterStatusCallback statusCallback;
        public StoreUserDataAsyncTask(User user, GetRegisterStatusCallback statusCallback){
            Log.d("sign up","async task");
            result = new String[2];
            result[0] = "";
            result[1] = "";
            this.user = user;
            this.statusCallback = statusCallback;
        }
        @Override
        protected String[] doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("firstName", user.firstName));
            inputData.add(new BasicNameValuePair("lastName", user.lastName));
            inputData.add(new BasicNameValuePair("email", user.email));
            inputData.add(new BasicNameValuePair("password", user.password));

            Log.d("sign up", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"register.php");
            Log.d("sign up", "connected");

            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("sign up", strEntity);
                JSONObject j = new JSONObject(strEntity);
                result[0] = ""+j.getBoolean("emailExist");
                result[1] = j.getString("message");


            } catch(Exception e){
                e.printStackTrace();
            }
            Log.d("sign up", "returned");
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            Log.d("login", "post execute");
            statusCallback.done(result);
        }


    }
    public class StoreFacebookUserDataAsyncTask extends AsyncTask<Void, Void, String[]> {
        String[] result;
        String firstName;
        String lastName;
        String facebookId;
        GetRegisterStatusCallback statusCallback;

        public StoreFacebookUserDataAsyncTask(String firstName, String lastName, String facebookId, GetRegisterStatusCallback statusCallback) {
            Log.d("sign up", "async task");
            result = new String[1];
            result[0] = "";
            this.firstName = firstName;
            this.lastName = lastName;
            this.facebookId = facebookId;
            this.statusCallback = statusCallback;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("firstName", firstName));
            inputData.add(new BasicNameValuePair("lastName", lastName));
            inputData.add(new BasicNameValuePair("facebookId", facebookId));

            Log.d("sign up", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SocMedRegister.php");
            Log.d("sign up", "connected");

            try {
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("sign up", strEntity);
                JSONObject j = new JSONObject(strEntity);
                result[0] = j.getString("message");

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            Log.d("sign up", "returned");
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            Log.d("login", "post execute");
            statusCallback.done(result);
        }
    }

    public class ActiveUserAsyncTask extends AsyncTask<Void, Void, String[]>{
        String[] result;
        User user;
        String activationCode;
        GetRegisterStatusCallback statusCallback;
        public ActiveUserAsyncTask(User user, String activationCode,GetRegisterStatusCallback statusCallback){
            Log.d("active","async task");
            result = new String[2];
            result[0] = "";
            result[1] = "";
            this.user = user;
            this.activationCode = activationCode;
            this.statusCallback = statusCallback;
        }
        @Override
        protected String[] doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("email", user.email));
            inputData.add(new BasicNameValuePair("password", user.password));
            inputData.add(new BasicNameValuePair("status", activationCode));
            Log.d("active", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"activeUser.php");
            Log.d("active", "connected");

            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("active", strEntity);
                JSONObject j = new JSONObject(strEntity);
                result[0] = ""+j.getBoolean("isActive");
                result[1] = j.getString("message");


            } catch(Exception e){
                e.printStackTrace();
            }
            Log.d("sign up", "returned");
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            Log.d("login","post execute");
            statusCallback.done(result);
        }
    }

    public class GetFeedAsyncTask extends AsyncTask<Void, Void, List<PostItem>>{
        GetFeedCallback callback;
        int numFeed;
        String userId;
        public GetFeedAsyncTask(int numFeed, String userId , GetFeedCallback callback){
            Log.d("feed","async task");
            this.numFeed = numFeed;
            this.callback = callback;
            this.userId = userId;
        }
        @Override
        protected List<PostItem> doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("numFeed", ""+numFeed));
            inputData.add(new BasicNameValuePair("userId", userId));
            Log.d("feed", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"getFeed.php");
            Log.d("feed", "connected");
            List<PostItem> listPostItem = new ArrayList<>();
            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("feed", strEntity);
                JSONObject json = new JSONObject(strEntity);
                JSONArray jsonArray = json.getJSONArray("post");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonobject = jsonArray.getJSONObject(i);
                    String id = jsonobject.getString("id");
                    String link = jsonobject.getString("link");
                    String linkSmall = jsonobject.getString("linkSmall");
                    String description = jsonobject.getString("description");
                    String name = jsonobject.getString("name");
                    String time = jsonobject.getString("time");
                    int totalLike = jsonobject.getInt("totalLike");
                    boolean isLike = jsonobject.getBoolean("isLike");
                    String address = jsonobject.getString("address");
                    double latitude = jsonobject.getDouble("latitude");
                    double longitude = jsonobject.getDouble("longitude");
                    PostItem postItem = new PostItem(id, null, name, time, link, linkSmall, description, totalLike, isLike, address, latitude, longitude);
                    listPostItem.add(postItem);
                }

            } catch(Exception e){
                Log.d("feed", "exception");
                e.printStackTrace();
                return null;
            }
            Log.d("feed", "returned");
            return listPostItem;
        }

        @Override
        protected void onPostExecute(List<PostItem> result) {
            super.onPostExecute(result);
            Log.d("login","post execute");
            callback.done(result);
        }
    }

    public class GetUpdateFeedAsyncTask extends AsyncTask<Void, Void, List<PostItem>>{
        GetFeedCallback callback;
        int flagId;
        int requestCode;
        int numFeed;
        String userId;

        public GetUpdateFeedAsyncTask(int numFeed ,int flagId, int requestCode, String userId , GetFeedCallback callback){
            Log.d("feed","async task");
            this.numFeed = numFeed;
            this.flagId = flagId;
            this.requestCode = requestCode;
            this.callback = callback;
            this.userId = userId;
        }
        @Override
        protected List<PostItem> doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("numFeed", ""+numFeed));
            inputData.add(new BasicNameValuePair("flagId", ""+flagId));
            inputData.add(new BasicNameValuePair("requestCode", ""+requestCode));
            inputData.add(new BasicNameValuePair("userId", userId));

            Log.d("feed", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"updateFeed.php");
            Log.d("feed", "connected");
            List<PostItem> listPostItem = new ArrayList<>();
            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("feed", strEntity);
                JSONObject json = new JSONObject(strEntity);
                JSONArray jsonArray = json.getJSONArray("post");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonobject = jsonArray.getJSONObject(i);
                    String id = jsonobject.getString("id");
                    String link = jsonobject.getString("link");
                    String linkSmall = jsonobject.getString("link");
                    String description = jsonobject.getString("description");
                    String name = jsonobject.getString("name");
                    String time = jsonobject.getString("time");
                    int totalLike = jsonobject.getInt("totalLike");
                    boolean isLike = jsonobject.getBoolean("isLike");
                    String address = jsonobject.getString("address");
                    double latitude = jsonobject.getDouble("latitude");
                    double longitude = jsonobject.getDouble("longitude");
                    PostItem postItem = new PostItem(id, null, name, time, link, linkSmall, description, totalLike, isLike, address, latitude, longitude);
                    listPostItem.add(postItem);
                }


            } catch(Exception e){
                Log.d("feed", "exception");
                e.printStackTrace();
                return null;
            }
            Log.d("feed", "returned");
            return listPostItem;
        }

        @Override
        protected void onPostExecute(List<PostItem> result) {
            super.onPostExecute(result);
            Log.d("login","post execute");
            callback.done(result);
        }
    }

    public class ChangePasswordAsyncTask extends AsyncTask<Void, Void, String[]>{
        String[] result;
        String id;
        String newPassword;
        String oldPassword;
        GetRegisterStatusCallback statusCallback;
        public ChangePasswordAsyncTask(String id, String oldPassword, String newPassword,  GetRegisterStatusCallback statusCallback){
            Log.d("change","async task");
            result = new String[2];
            result[0] = "";
            result[1] = "";
            this.id = id;
            this.newPassword = newPassword;
            this.oldPassword = oldPassword;
            this.statusCallback = statusCallback;
        }
        @Override
        protected String[] doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("id", id));
            inputData.add(new BasicNameValuePair("newPassword", newPassword));
            inputData.add(new BasicNameValuePair("oldPassword", oldPassword));
            Log.d("change", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"changePassword.php");
            Log.d("change", "connected");

            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("change", strEntity);
                JSONObject j = new JSONObject(strEntity);
                result[0] = ""+j.getBoolean("isSuccess");
                result[1] = j.getString("message");

            } catch(Exception e){
                e.printStackTrace();
            }
            Log.d("change", "returned");
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            Log.d("change","post execute");
            statusCallback.done(result);
        }
    }

    public class LikeAsyncTask extends AsyncTask<Void, Void, String>{
        String message;
        boolean isLike;
        String userId;
        String postId;
        GetLikeCallback callback;
        public LikeAsyncTask(boolean isLike, String userId, String postId, GetLikeCallback callback){
            Log.d("like","async task");
            this.isLike = isLike;
            this.userId = userId;
            this.postId = postId;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... params) {
            ArrayList<NameValuePair> inputData = new ArrayList<>();
            inputData.add(new BasicNameValuePair("isLike", ""+isLike));
            inputData.add(new BasicNameValuePair("userId", userId));
            inputData.add(new BasicNameValuePair("postId", postId));
            Log.d("like", "do in background");
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"like.php");
            Log.d("like", "connected");

            try{
                post.setEntity(new UrlEncodedFormEntity(inputData));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("like", strEntity);
                JSONObject j = new JSONObject(strEntity);
                message = j.getString("message");
            } catch(Exception e){
                e.printStackTrace();
                return null;
            }
            Log.d("like", "returned");
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            Log.d("like","post execute");
            callback.done(message);
        }
    }

    public class GetMarkerAsyncTask extends AsyncTask<Void, Void, ArrayList<Marker>>{
        GetMarkerCallback callback;

        public GetMarkerAsyncTask(GetMarkerCallback callback){
            this.callback = callback;
            Log.d("feed","async task");
        }
        @Override
        protected ArrayList<Marker> doInBackground(Void... params) {
            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIMEOUT);

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS+"getMarker.php");
            Log.d("feed", "connected");
            ArrayList<Marker> listMarker = new ArrayList<>();
            try{
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String strEntity = EntityUtils.toString(entity);
                Log.d("feed", strEntity);
                JSONObject json = new JSONObject(strEntity);
                JSONArray jsonArray = json.getJSONArray("post");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonobject = jsonArray.getJSONObject(i);
                    String id = jsonobject.getString("id");
                    double latitude = jsonobject.getDouble("latitude");
                    double longitude = jsonobject.getDouble("longitude");
                    Marker marker = new Marker(id, latitude, longitude);
                    listMarker.add(marker);
                }
            } catch(Exception e){
                Log.d("feed", "exception");
                e.printStackTrace();
                return null;
            }
            Log.d("feed", "returned");
            return listMarker;
        }

        @Override
        protected void onPostExecute(ArrayList<Marker> result) {
            super.onPostExecute(result);
            Log.d("login","post execute");
            callback.done(result);
        }
    }
}
