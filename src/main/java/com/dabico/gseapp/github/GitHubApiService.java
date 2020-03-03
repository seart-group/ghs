package com.dabico.gseapp.github;

import com.dabico.gseapp.util.DateInterval;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.IOException;

@NoArgsConstructor
public class GitHubApiService extends HTTPService {

    //TODO configure application.properties access
    private static final String accessToken = "56583668e32b73702785a85900975d1ceccf15d5";

    public Response gitHubSearchRepositories(String language, DateInterval interval, Integer page) throws IOException {
        Request request = new Request.Builder()
                                     .url("https://api.github.com/search/repositories?q=" +
                                                                                     "language:" + language +
                                                                                     "+created:" + interval.toString() +
                                                                                     "+fork:true" +
                                                                                     "&page=" + page +
                                                                                     "&per_page=100")
                                     .addHeader("Authorization", accessToken)
                                     .addHeader("Accept", "application/vnd.github.v3+json")
                                     .build();

        Call call = client.newCall(request);
        return call.execute();
    }
}
