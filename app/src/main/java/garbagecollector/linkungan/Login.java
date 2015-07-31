package garbagecollector.linkungan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

//TODO:tambah twitter, facebook login

/**
 * Activity untuk login
 */
public class Login extends ActionBarActivity implements View.OnClickListener {
    //email input
    private EditText etEmail;
    //password input
    private EditText etPassword;
    //button login
    private Button btnLogin;
    //sharedPreferences aplikasi
    private UserLocalStore userLocalStore;
    //context dari aplikasi ini (bbrp inner class method butuh context)
    private Context context;
    //untuk handle connect di server
    private ServerRequest serverRequest;
    private boolean firstBackPressed;
    CallbackManager callbackManager;
    private ProgressDialog progressDialog;
    public final static int NORMAL_LOGIN_METHOD = 0;
    public final static int FACEBOOK_LOGIN_METHOD = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();
        context = this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("login", "success facebook");
                progressDialog.show();
                GraphRequest request = GraphRequest.newMeRequest( AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {
                                final String id  = Profile.getCurrentProfile().getId();
                                final String firstName = Profile.getCurrentProfile().getFirstName();
                                final String lastName = Profile.getCurrentProfile().getLastName();
                                Log.d("login", id+" "+firstName+" "+lastName);

                                serverRequest.fetchUserFacebookDataInBackground(id, new GetUserCallback() {
                                    @Override
                                    public void done(User returnedUser) {
                                        if(returnedUser != null){
                                            progressDialog.dismiss();
                                            if(returnedUser.id.equals("-1")){
                                                showErrorMessage("Connection Error");
                                            } else {
                                                startSession(returnedUser, FACEBOOK_LOGIN_METHOD);
                                            }
                                        } else {
                                            Log.d("login", "storing facebook user"+firstName+" "+lastName+" "+id);
                                            storeFacebookUser(firstName, lastName, id);
                                        }
                                    }
                                });
                            }
                        });

                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d("login", "cancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("login", e.toString());
            }
        });

        //inisialisasi server connect
        serverRequest = new ServerRequest(this);

        //inisialisasi tampilan
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>Login</font>"));

        etEmail = (EditText) findViewById(R.id.etStoredEmail);
        etPassword = (EditText) findViewById(R.id.etStoredPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        //inisialisasi sharedPreferences
        userLocalStore = new UserLocalStore(this);

        //jika login dipanggil dari activity lain, tampilin alert dialog

        Intent i = getIntent();
        boolean isFromSignUp = i.getBooleanExtra("isFromSignUp", false);
        if(isFromSignUp){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Sign Up success, please check your email to get activation code");
            dialog.setPositiveButton("Ok", null);
            dialog.show();
        }
        boolean isFromChangePassword = i.getBooleanExtra("isFromChangePassword", false);
        if(isFromChangePassword){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Password successfully changed");
            dialog.setPositiveButton("Ok", null);
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Apabila button login diclick
     * @param v
     */
    @Override
    public void onClick(View v) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if(email.equals("") || password.equals("")){
            showErrorMessage("Please fill email and password");
            etPassword.setText("");
            return;
        }
        User sentUser = new User("","","",email, password, "");
        Log.d("login", email + " " + password);
        //authentication ke server
        loginUser(sentUser);
    }
    public void storeFacebookUser(String firstName, String lastName, final String facebookId){
        serverRequest.storeFacebookUserDataAsyncTask(firstName, lastName, facebookId, new GetRegisterStatusCallback() {
            @Override
            public void done(String[] result) {
                if(result == null){
                    progressDialog.dismiss();
                    showErrorMessage("Connection Error");
                } else{
                    if(result[0].equals("0")){
                        serverRequest.fetchUserFacebookDataInBackground(facebookId, new GetUserCallback() {
                            @Override
                            public void done(User returnedUser) {
                                if(returnedUser != null){
                                    progressDialog.dismiss();
                                    if(returnedUser.id.equals("-1")){
                                        showErrorMessage("Connection Error");
                                    } else {
                                        startSession(returnedUser, FACEBOOK_LOGIN_METHOD);
                                    }
                                }
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        showErrorMessage("Error while Registering Facebook User");
                    }
                }
            }
        });
    }

    private void loginUser(User sentUser){
        //fetch user data, jika ada maka user login
        serverRequest.fetchUserDataInBackground(sentUser, new GetUserCallback() {
            /**
             * implementasi abstract method, mau lakukan apa setelah proses fetch data selesai
             * @param returnedUser
             */
            @Override
            public void done(final User returnedUser) {
                //jika user tidak ditemukan
                if(returnedUser == null){
                    Log.d("login", "display error");
                    etPassword.setText("");
                    showErrorMessage("Incorrect Email or Password");
                } else {
                    //jika koneksi berhasil
                    if(!returnedUser.id.equals("-1")) {
                        //jika status user = 0 (berarti udah aktivasi)
                        if (returnedUser.status.equals("0")) {
                            Log.d("login", "start session");
                            //berhasil di otentikasi
                            startSession(returnedUser, NORMAL_LOGIN_METHOD);
                        } else {
                            //belum diaktivasi, tampilkan alert dialog meminta kode aktivasi

                            final EditText code = new EditText(context);
                            code.setInputType(InputType.TYPE_CLASS_NUMBER);
                            code.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                            code.setGravity(Gravity.CENTER);
                            code.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
                            code.setHint("");

                            new AlertDialog.Builder(context)
                                    .setTitle("Active your account")
                                    .setMessage("Your account is not activated. Please Put the activation code")
                                    .setView(code)
                                    .setPositiveButton("Active", new DialogInterface.OnClickListener() {
                                        //listener tombol active ditekan
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            String activationCode = code.getText().toString();
                                            Log.d("active", activationCode);
                                            serverRequest.activeUserInBackGround(returnedUser, activationCode, new GetRegisterStatusCallback() {
                                                /**
                                                 * Apabila proses active user selesai dari server
                                                 *
                                                 * result[0] berisi isSuccess
                                                 * result[1] berisi message
                                                 * @param result
                                                 */
                                                @Override
                                                public void done(String[] result) {
                                                    //jika sukses aktivasi
                                                    if (result[0].equalsIgnoreCase("true")) {
                                                        returnedUser.status = "" + 0;
                                                        startSession(returnedUser, NORMAL_LOGIN_METHOD);
                                                    } else {
                                                        etPassword.setText("");
                                                        //jika result[1] == 1 , maka kode aktivasi salah
                                                        if (result[1].equals("1")) {
                                                            showErrorMessage("Activation code incorrect");
                                                            //error koneksi atau error di SQL
                                                        } else {
                                                            showErrorMessage("Error while Activating account");
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .show();
                        }
                    } else {
                        showErrorMessage("Connection Error");
                        etPassword.setText("");
                    }
                }

            }
        });
    }

    /**
     * Tampilin error dialog
     * @param message
     */
    private void showErrorMessage(String message){
           AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
           dialog.setMessage(message);
           dialog.setPositiveButton("Ok", null);
           dialog.show();
    }

    /**
     * Method untuk mengubah login status dan menuju ke home
     * @param returnedUser
     */
    private void startSession(User returnedUser, int loginMethod){
        //set di sharedPreferences data login user
        userLocalStore.storeUserData(returnedUser);
        //set boolean bahwa user telah login
        userLocalStore.setUserLoggedIn(true);
        userLocalStore.setLoginMethod(loginMethod);
        Log.d("login","ok");
        //start activity home
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent,0);
        overridePendingTransition(0,0);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //jika sign up di klik, menuju ke sign up activity
        if (id == R.id.action_Sign_In) {
            Intent intent = new Intent(this, SignUp.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent,0);
            overridePendingTransition(0,0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(firstBackPressed){
            this.finish();
            System.exit(0);
        } else {
            firstBackPressed = true;
            Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firstBackPressed = false;
                }
            }, 3000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(userLocalStore.isLoggedIn()){
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent,0);
            overridePendingTransition(0,0);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(userLocalStore.isLoggedIn()){
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent,0);
            overridePendingTransition(0,0);
            finish();
        }
    }
}
