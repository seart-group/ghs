package com.dabico.gseapp.github;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
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

    protected Response getPageAsHTML(String pageURL) throws IOException{
        Request request = new Request.Builder().url(pageURL).addHeader("Accept","text/html").build();
        Call call = client.newCall(request);
        return call.execute();
    }
}
