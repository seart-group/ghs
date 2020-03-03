package com.dabico.gseapp.github;

import lombok.Getter;
import lombok.NoArgsConstructor;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class WebpageCrawlerService extends HTTPService {
    public Response crawlPage(String pageURL){
        Request request = new Request.Builder()
                                     .url(pageURL)
                                     .addHeader("Accept","text/html")
                                     .build();
        Call call = client.newCall(request);
        try {
            return call.execute();
        } catch (IOException e) {
            //TODO Better exception handling in case call fails
            e.printStackTrace();
        }
        return null;
    }
}
