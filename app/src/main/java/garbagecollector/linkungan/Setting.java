package garbagecollector.linkungan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;

//TODO: change profile (picture, data)
public class Setting extends ActionBarActivity implements View.OnClickListener {
    private Button btnLogout;
    private Button btnChangePassword;
    private UserLocalStore userLocalStore;
    private ServerRequest serverRequest;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>Setting</font>"));
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        serverRequest = new ServerRequest(this);
        userLocalStore = new UserLocalStore(this);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnChangePassword = (Button) findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(this);
        if(userLocalStore.getLoginMethod() == Login.NORMAL_LOGIN_METHOD){
            btnChangePassword.setVisibility(View.VISIBLE);
        }
        btnLogout.setOnClickListener(this);

    }

    /**
     * Method yang akan dijalankan pada saat activity ini dibuat
     */
    @Override
    protected void onStart(){
        super.onStart();
        //check apabila keadaan login
        if(!new UserLocalStore(this).isLoggedIn()){
            //kembali ke login activity
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //check apabila keadaan login
        if(!new UserLocalStore(this).isLoggedIn()){
            //kembali ke login activity
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.btnLogout :
                userLocalStore.setUserLoggedIn(false);
                if(userLocalStore.getLoginMethod() == Login.FACEBOOK_LOGIN_METHOD){
                    AccessToken.setCurrentAccessToken(null);
                }
                userLocalStore.clearUserData();
                Intent logoutIntent = new Intent(this, Login.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                break;
            case R.id.btnChangePassword:
                viewChangePasswordDialog();
                break;
        }
    }

    /**
     * Dialog untuk change password
     */
    public void viewChangePasswordDialog(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Change Password");
        final ServerRequest serverRequest = new ServerRequest(this);
        //set layout/frame
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tvOldPassword = new TextView(this);
        tvOldPassword.setTextSize(20);
        tvOldPassword.setText("Old Password");
        layout.addView(tvOldPassword);

        final EditText etOldPassword = new EditText(this);
        etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOldPassword);

        final TextView tvNewPassword = new TextView(this);
        tvNewPassword.setTextSize(20);
        tvNewPassword.setText("New Password");
        layout.addView(tvNewPassword);

        final EditText etNewPassword = new EditText(this);
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        final TextView tvNewPasswordConfirm = new TextView(this);
        tvNewPasswordConfirm.setTextSize(20);
        tvNewPasswordConfirm.setText("Confirm New Password");
        layout.addView(tvNewPasswordConfirm);

        final EditText etNewPasswordConfirm = new EditText(this);
        etNewPasswordConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPasswordConfirm);

        dialog.setView(layout);
        //Apabila tombol change ditekan
        dialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                User user = userLocalStore.getLoggedInUser();
                //validasi input
                if(etOldPassword.getText().toString().equals(user.password)){
                    if(etNewPassword.getText().toString().length()>=8){
                        if(etNewPassword.getText().toString().equals(etNewPasswordConfirm.getText().toString())){
                            serverRequest.changePasswordInBackground(user.id, user.password, etNewPassword.getText().toString(), new GetRegisterStatusCallback() {
                                @Override
                                public void done(String[] result) {
                                    //result[0] = isSuccess : apakah sukses change password
                                    if(result[0].equals("false")){
                                        progressDialog.dismiss();
                                        showAlert("Error while changing password");
                                    } else if(result[0].equals("true")){
                                        progressDialog.dismiss();
                                        //proses berhasil, mulai activity logout
                                        changeAndLogout();
                                    } else {
                                        //unidentified error (SQL atau connection error)
                                        progressDialog.dismiss();
                                        showAlert("Error while changing password");
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            showAlert("Confirm password incorrect!");
                        }
                    } else {
                        progressDialog.dismiss();
                        showAlert("Password Must be >= 8 digits");
                    }
                } else {
                    progressDialog.dismiss();
                    showAlert("Old password incorrect!");
                }

            }
        });
        dialog.setNegativeButton("Cancel",null);
        dialog.show();
    }

    /**
     * Logout setelah ganti password
     */
    public void changeAndLogout(){
        Intent intent = new Intent(this, Login.class);
        intent.putExtra("isFromChangePassword", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, 0);
        overridePendingTransition(0, 0);
        userLocalStore.setUserLoggedIn(false);
        userLocalStore.clearUserData();
        finish();
    }

    /**
     * Menampilkan alert dialog
     * @param message
     */
    private void showAlert(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", null);
        dialog.show();
    }
}
