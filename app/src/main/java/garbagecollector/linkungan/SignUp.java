package garbagecollector.linkungan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity sign Up
 */
public class SignUp extends ActionBarActivity implements View.OnClickListener {
    //input
    private EditText etStoredFirstName;
    private EditText etStoredLastName;
    private EditText etStoredEmail;
    private EditText etStoredPassword;
    private EditText etStoredConfirmPassword;
    private ServerRequest serverRequest;
    private Button btnSignUp;
    private Context context;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //set Action bar
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>Sign Up</font>"));
        //set input (textView and button)
        etStoredFirstName = (EditText) findViewById(R.id.etStoredFirstName);
        etStoredLastName = (EditText) findViewById(R.id.etStoredLastName);
        etStoredEmail = (EditText) findViewById(R.id.etStoredEmail);
        etStoredPassword = (EditText) findViewById(R.id.etStoredPassword);
        etStoredConfirmPassword = (EditText) findViewById(R.id.etStoredConfirmPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
        //set Server, context
        serverRequest = new ServerRequest(this);
        context = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Signing Up");
        progressDialog.setMessage("Please wait...");
    }


    private void showErrorMessage(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(SignUp.this);
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", null);
        dialog.show();
    }



    @Override
    public void onClick(View v) {
        progressDialog.show();
        String firstName = etStoredFirstName.getText().toString().trim();
        String lastName = etStoredLastName.getText().toString().trim();
        String email = etStoredEmail.getText().toString().trim();
        String password = etStoredPassword.getText().toString();
        String confirmPassword = etStoredConfirmPassword.getText().toString();
        //check validasi data
        if(isValid(firstName, lastName, email, password, confirmPassword)) {
            User sentUser = new User("",firstName, lastName, email, password,"");
            serverRequest.StoreUserDataInBackground(sentUser, new GetRegisterStatusCallback() {
                @Override
                public void done(String[] result) {
                    //true jika email sudah ada
                    if(result[0].equalsIgnoreCase("true")){
                        etStoredPassword.setText("");
                        etStoredConfirmPassword.setText("");
                        if(result[1].equals("1")){
                            progressDialog.dismiss();
                            showErrorMessage("Email Already Exist");
                        } else {
                            progressDialog.dismiss();
                            showErrorMessage("Error while Signing Up");
                        }
                        //false jika email valid
                    } else if (result[0].equalsIgnoreCase("false")){
                        Intent intent = new Intent(context, Login.class);
                        intent.putExtra("isFromSignUp", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivityForResult(intent,0);
                        overridePendingTransition(0,0);
                        finish();
                     } else {
                        //error koneksi
                        progressDialog.dismiss();
                        showErrorMessage("Error while Signing Up");
                    }
                }
            });
        } else {
            etStoredPassword.setText("");
            etStoredConfirmPassword.setText("");
        }
    }

    //validasi data
    public boolean isValid(String firstName, String lastName, String email, String password, String confirmPassword){
        //first name minimal 4 digit
        if(firstName.length()<4) {
            progressDialog.dismiss();
            showErrorMessage("First name must be >=4 characters");
            return false;
        }
        //last name minimal 4 digit
        if(lastName.length()<4) {
            progressDialog.dismiss();
            showErrorMessage("Last name must be >=4 characters");
            return false;
        }
        //check pattern email
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        if(email.length()==0 || !m.matches()){
            progressDialog.dismiss();
            showErrorMessage("Email not valid");
            return false;
        }
        //password minimal 8 digit
        if(password.length()<8){
            progressDialog.dismiss();
            showErrorMessage("Password must be >=8 characters");
            return false;
        }
        //check kesamaan password dan konfirmasi password
        if(!password.equalsIgnoreCase(confirmPassword)){
            progressDialog.dismiss();
            showErrorMessage("Confirm password error");
            return false;
        }

        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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


}
