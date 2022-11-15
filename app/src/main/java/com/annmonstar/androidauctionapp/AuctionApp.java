package com.annmonstar.androidauctionapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class AuctionApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
