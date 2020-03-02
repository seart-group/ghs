package com.dabico.gseapp.github;

import com.dabico.gseapp.util.DateInterval;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.IOException;

@NoArgsConstructor
public class GitHubApiService extends HTTPService {

    public Response gitHubSearchRepositories(String language, DateInterval interval,Integer pageNumber){
        Request request = new Request.Builder()
                .url("https://api.github.com/search/repositories?q=" +
                        "language:" + language +
                        "+created:" + interval.toString() +
                        "+fork:true" +
                        "&page=" + pageNumber +
                        "&per_page=100")
                .addHeader("Authorization", "56583668e32b73702785a85900975d1ceccf15d5")
                .addHeader("Accept", "application/vnd.github.v3+json")
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
