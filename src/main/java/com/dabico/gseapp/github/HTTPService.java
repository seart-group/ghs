package com.dabico.gseapp.github;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public abstract class HTTPService {
    protected OkHttpClient client;

    protected HTTPService(){
        this.client = new OkHttpClient.Builder()
                                      .connectTimeout(1, TimeUnit.MINUTES)
                                      .writeTimeout(1, TimeUnit.MINUTES)
                                      .readTimeout(1, TimeUnit.MINUTES)
                                      .build();
    }
}
