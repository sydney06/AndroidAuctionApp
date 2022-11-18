package com.annmonstar.androidauctionapp.ui.utils;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String mAuthToken;

    public AuthInterceptor(String authToken) {
        mAuthToken = authToken;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request  = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + mAuthToken)
                .addHeader("Content-Type", "application/json")
                .build();
//        Log.d("TAG-AUTH", "Bearer " + mAuthToken);
        return chain.proceed(request);
    }
}