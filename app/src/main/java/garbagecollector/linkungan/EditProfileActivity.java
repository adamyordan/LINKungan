package garbagecollector.linkungan;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class EditProfileActivity extends ActionBarActivity implements View.OnClickListener{

    EditText et_firstName;
    EditText et_lastName;
    EditText et_email;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Setting warna dan title di action bar
        ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008A00")));
        ab.setTitle(Html.fromHtml("<font color='#ffffff'>Edit Profile</font>"));

        userLocalStore = new UserLocalStore(this);
        User loggedInUser = userLocalStore.getLoggedInUser();

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
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.button_update:
                //todo: update the user parameter in server
                break;
        }
    }
}
