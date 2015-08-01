package garbagecollector.linkungan;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class EditProfileActivity extends ActionBarActivity implements View.OnClickListener{

    EditText et_firstName;
    EditText et_lastName;
    EditText et_email;
    ServerRequest serverRequest;

    UserLocalStore userLocalStore;
    User loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Setting warna dan title di action bar
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#fffffe'>Edit Profile</font>"));

        serverRequest = new ServerRequest(this);
        userLocalStore = new UserLocalStore(this);
        loggedInUser = userLocalStore.getLoggedInUser();

        et_firstName = (EditText) findViewById(R.id.editText_FirstName);
        et_lastName = (EditText) findViewById(R.id.editText_lastName);
        et_email = (EditText) findViewById(R.id.editText_email);

        et_firstName.setText(loggedInUser.firstName);
        et_lastName.setText(loggedInUser.lastName);
        et_email.setText(loggedInUser.email);

        if(userLocalStore.getLoginMethod() == Login.FACEBOOK_LOGIN_METHOD){
            et_email.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.textView_email)).setVisibility(View.GONE);
        }

        ((Button) findViewById(R.id.button_update)).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.button_update:
                serverRequest.updateProfileInBackground(loggedInUser.id, et_firstName.getText().toString(), et_lastName.getText().toString(), et_email.getText().toString(), new GetRegisterStatusCallback() {
                    @Override
                    public void done(String[] result) {
                        Log.d("adam", loggedInUser.id + et_firstName.getText().toString() + et_email.getText().toString());
                    }
                });
                break;
        }
    }
}
