package garbagecollector.linkungan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.Profile;

/**
 * Created by Jefly on 7/6/2015 .
 * Class yang mengatur sharedPreferences, klo di php mirim kyak $_SERVER[]
 */
public class UserLocalStore {
    //key dari sharedPreferences
    public static final String SP_KEY = "userDetails";
    SharedPreferences userDB;

    public UserLocalStore(Context context){
        userDB = context.getSharedPreferences(SP_KEY,0);
    }

    //store user data apabila login, data-data disimpan di sharedPreferences
    public void storeUserData(User user){
        Log.d("login", "store user data");
        SharedPreferences.Editor spEditor = userDB.edit();
        spEditor.putString("id", user.id);
        spEditor.putString("firstName", user.firstName);
        spEditor.putString("lastName", user.lastName);
        spEditor.putString("email", user.email);
        spEditor.putString("password", user.password);
        spEditor.putString("status", user.status);
        spEditor.commit();
        Log.d("login", "store user data complete");
    }

    //Mengembalikan storedData dari loggedInUser
    public User getLoggedInUser(){
        String id = userDB.getString("id","");
        String firstName = userDB.getString("firstName","");
        String lastName = userDB.getString("lastName", "");
        String email = userDB.getString("email","");
        String password = userDB.getString("password","");
        String status = userDB.getString("status","");
        return new User(id,firstName, lastName, email,password, status);
    }

    //set boolean login
    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor spEditor = userDB.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.commit();
        Log.d("login","set Login boolean complete");
    }

    public void setLoginMethod(int method){
        SharedPreferences.Editor spEditor = userDB.edit();
        spEditor.putInt("loginMethod", method);
        spEditor.commit();
    }
    public int getLoginMethod(){
        return userDB.getInt("loginMethod",0);
    }

    public boolean isLoggedIn(){
        if(userDB.getInt("loginMethod", 0) == Login.NORMAL_LOGIN_METHOD) {
            return userDB.getBoolean("loggedIn", false);
        } else {
            return AccessToken.getCurrentAccessToken() != null && userDB.getBoolean("loggedIn", false);
        }
    }

    //Clear User data di sharedPreferences
    public void clearUserData(){
        SharedPreferences.Editor spEditor = userDB.edit();
        spEditor.clear();
        spEditor.commit();
    }
}
