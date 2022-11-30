package com.annmonstar.androidauctionapp.ui.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.HomeActivity;
import com.annmonstar.androidauctionapp.ui.admin.AdminActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    AlertDialog dialog_verifying, profile_dialog;
    private Toolbar mToolbar;
    private EditText mLoginEmail;
    private EditText mLoginPassword;
    private Button mLogin_btn, mSign_up;
    private Context mContext;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Log In");

        mAuth = FirebaseAuth.getInstance();

        mLogin_btn = findViewById(R.id.lg_login);
        mSign_up = findViewById(R.id.lg_signup);

        mLoginEmail = findViewById(R.id.lg_email);
        mRegProgress = new ProgressDialog(LoginActivity.this);
        mLoginPassword = findViewById(R.id.lg_pass);

        mContext = this;


        mLogin_btn.setOnClickListener(view -> {

            String email = mLoginEmail.getText().toString();
            String password = mLoginPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(mContext, "You can't leave fields empty", Toast.LENGTH_SHORT).show();
            } else {
                mRegProgress.setTitle("Logging in");
                mRegProgress.setMessage("Please wait while we log you into your account!");
                mRegProgress.setCanceledOnTouchOutside(false);
                mRegProgress.show();
                loginUser(email, password);
            }
        });

        mSign_up.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Log.e("rg", "onComplete: Failed=" + Objects.requireNonNull(task.getException()).getMessage());
                String current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                currentUser.getIdToken(true).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        mUserDatabase.child(current_user_id).child("device_token").setValue(Objects.requireNonNull(task1.getResult()).getToken()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mRegProgress.dismiss();
                                getUserDetails(currentUser);
                            }
                        });

                    } else {
                        Log.e("TAG", "exception=" + Objects.requireNonNull(task1.getException()));
                        Toast.makeText(LoginActivity.this, "Error - " + task1.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }else {
                Toast.makeText(LoginActivity.this, "Error, couldn't sign in. ", Toast.LENGTH_SHORT).show();

            }



        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getUserDetails(currentUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseDatabase.getInstance().goOnline();

    }

    private void getUserDetails(FirebaseUser currentUser) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                String type = Objects.requireNonNull(snapshot.child("admin").getValue()).toString();
                Intent intent;
                if (type.equals("true")) {
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
                Toast.makeText(LoginActivity.this, "Log in success.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(LoginActivity.this, "User does not exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}