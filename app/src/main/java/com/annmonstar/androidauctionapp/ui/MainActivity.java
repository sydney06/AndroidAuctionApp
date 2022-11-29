package com.annmonstar.androidauctionapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.annmonstar.androidauctionapp.R;
import com.annmonstar.androidauctionapp.ui.authentication.LoginActivity;
import com.annmonstar.androidauctionapp.ui.authentication.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegBtn = (Button) findViewById(R.id.register);
        mLoginBtn = (Button) findViewById(R.id.login);

        mRegBtn.setOnClickListener(view -> {
            Intent reg_intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(reg_intent);
        });

        mLoginBtn.setOnClickListener(view -> {
            Intent login_intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login_intent);
        });


    }
}