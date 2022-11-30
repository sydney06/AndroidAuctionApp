package com.annmonstar.androidauctionapp.ui.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.HomeActivity;
import com.annmonstar.androidauctionapp.ui.admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private final int i = 0;
    AlertDialog dialog_verifying, profile_dialog;
    private EditText mDisplayName, mEmail, mPhoneNumber, mPassword, mCity;
    private Button mCreateBtn;
    private DatabaseReference mDatabase;
    //ProgressDialog
    private ProgressDialog mRegProgress;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private Switch adminSwitch;

    public static boolean verifyPhoneNumber(String phone) {

        if (phone.equals("")) {
            return false;
        }

        if (phone.length() != 10 || !phone.startsWith("0")) {
            String p = phone.replaceFirst("^0", "254");
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mRegProgress = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mRegProgress = new ProgressDialog(this);
        mDisplayName = (EditText) findViewById(R.id.reg_name);
        mEmail = (EditText) findViewById(R.id.reg_email);
        mPhoneNumber = findViewById(R.id.reg_phone_number);
        mPassword = (EditText) findViewById(R.id.reg_pass);
        mCity = (EditText) findViewById(R.id.reg_city);
        mCreateBtn = (Button) findViewById(R.id.register);

        adminSwitch = findViewById(R.id.admin_switch);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final String display_name = mDisplayName.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String city = mCity.getText().toString();
                final String phoneNumber = mPhoneNumber.getText().toString();
                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) || verifyPhoneNumber(phoneNumber)) {

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name, email, password, city, phoneNumber);
                  /* LayoutInflater inflater = getLayoutInflater();
                    View alertLayout= inflater.inflate(R.layout.processing_dialog,null);
                    AlertDialog.Builder show = new AlertDialog.Builder(RegisterActivity.this);
                    show.setView(alertLayout);
                    show.setCancelable(false);
                    dialog_verifying = show.create();
                    dialog_verifying.show();*/
                } else {
                    Toast.makeText(RegisterActivity.this, "Counter check your inputs", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // Android Fields

    }

    private void register_user(final String display_name, final String email, String password, final String city, final String phoneNumber) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                // dialog_verifying.dismiss();
                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                assert current_user != null;
                String uid = current_user.getUid();

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", display_name);
                userMap.put("email", email);
                userMap.put("phoneNumber", phoneNumber);
                userMap.put("city", city);
                userMap.put("image", "default");
                userMap.put("uid", uid);
                userMap.put("admin", adminSwitch.isChecked());


                mDatabase.child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent mainIntent;
                        if (task.isSuccessful()) {
                            if (adminSwitch.isChecked()) {
                                mainIntent = new Intent(RegisterActivity.this, AdminActivity.class);
                            } else {
                                mainIntent = new Intent(RegisterActivity.this, HomeActivity.class);
                            }
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    }
                });


            } else {
                // dialog_verifying.hide();
                String task_result = task.getException().getMessage().toString();
                mRegProgress.hide();
                Toast.makeText(RegisterActivity.this, task_result, Toast.LENGTH_LONG).show();

            }

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}