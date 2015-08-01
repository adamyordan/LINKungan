package garbagecollector.linkungan;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;


public class ProfileFragment extends Fragment implements View.OnClickListener{

    private Button btnLogout;
    private Button btnChangePassword;
    private Button btnEditProfile;
    private UserLocalStore userLocalStore;
    private ServerRequest serverRequest;
    private User loggedInUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        serverRequest = new ServerRequest(getActivity());
        userLocalStore = new UserLocalStore(getActivity());
        loggedInUser = userLocalStore.getLoggedInUser();

        btnLogout = (Button) v.findViewById(R.id.btnLogout);
        btnChangePassword = (Button) v.findViewById(R.id.btnChangePassword);
        btnEditProfile = (Button) v.findViewById(R.id.btnEditProfile);

        btnChangePassword.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);

        if(userLocalStore.getLoginMethod() == Login.NORMAL_LOGIN_METHOD){
            btnChangePassword.setVisibility(View.VISIBLE);
        } else {
            btnChangePassword.setVisibility(View.GONE);
        }

        TextView tv_username = (TextView) v.findViewById(R.id.textView_username);
        tv_username.setText(loggedInUser.firstName + " " + loggedInUser.lastName);

        return v;
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.btnLogout :
                userLocalStore.setUserLoggedIn(false);
                if(userLocalStore.getLoginMethod() == Login.FACEBOOK_LOGIN_METHOD){
                    AccessToken.setCurrentAccessToken(null);
                }
                userLocalStore.clearUserData();
                Intent logoutIntent = new Intent(getActivity(), Login.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().finish();
                break;
            case R.id.btnChangePassword:
                viewChangePasswordDialog();
                break;
            case R.id.btnEditProfile:
                Intent editIntent = new Intent(getActivity(), EditProfileActivity.class);
                startActivityForResult(editIntent, 0);
        }
    }

    /**
     * Dialog untuk change password
     */
    public void viewChangePasswordDialog(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Change Password");
        final ServerRequest serverRequest = new ServerRequest(getActivity());
        //set layout/frame
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);


        final TextView tvOldPassword = new TextView(getActivity());
        tvOldPassword.setTextSize(20);
        tvOldPassword.setText("Old Password");
        layout.addView(tvOldPassword);

        final EditText etOldPassword = new EditText(getActivity());
        etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOldPassword);

        final TextView tvNewPassword = new TextView(getActivity());
        tvNewPassword.setTextSize(20);
        tvNewPassword.setText("New Password");
        layout.addView(tvNewPassword);

        final EditText etNewPassword = new EditText(getActivity());
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        final TextView tvNewPasswordConfirm = new TextView(getActivity());
        tvNewPasswordConfirm.setTextSize(20);
        tvNewPasswordConfirm.setText("Confirm New Password");
        layout.addView(tvNewPasswordConfirm);

        final EditText etNewPasswordConfirm = new EditText(getActivity());
        etNewPasswordConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPasswordConfirm);

        dialog.setView(layout);
        //Apabila tombol change ditekan
        dialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                                        showAlert("Error while changing password");
                                    } else if(result[0].equals("true")){
                                        //proses berhasil, mulai activity logout
                                        changeAndLogout();
                                    } else {
                                        //unidentified error (SQL atau connection error)
                                        showAlert("Error while changing password");
                                    }
                                }
                            });
                        } else {
                            showAlert("Confirm password incorrect!");
                        }
                    } else {
                        showAlert("Password Must be >= 8 digits");
                    }
                } else {
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
        Intent intent = new Intent(getActivity(), Login.class);
        intent.putExtra("isFromChangePassword", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, 0);
        userLocalStore.setUserLoggedIn(false);
        userLocalStore.clearUserData();
        getActivity().finish();
    }

    /**
     * Menampilkan alert dialog
     * @param message
     */
    private void showAlert(String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", null);
        dialog.show();
    }

}
