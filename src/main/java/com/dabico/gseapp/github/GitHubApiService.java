package com.dabico.gseapp.github;

import com.dabico.gseapp.util.interval.DateInterval;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.*;
import org.apache.http.client.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.gson.JsonParser.parseString;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GitHubApiService {
    OkHttpClient client;

    @Autowired
    public GitHubApiService(){
        this.client = new OkHttpClient.Builder()
                                      .connectTimeout(1, TimeUnit.MINUTES)
                                      .writeTimeout(1, TimeUnit.MINUTES)
                                      .readTimeout(1, TimeUnit.MINUTES)
                                      .build();
    }

    public Response searchRepositories(String language, DateInterval interval, Integer page,
                                       String token, Boolean update) throws IOException, InterruptedException
    {
        Response response = makeAPICall(Endpoints.SEARCH_REPOS.getUrl() +
                                                "?q=language:" + language +
                                                (update ? "+pushed:" : "+created:") + interval +
                                                "+fork:true+is:public&page=" + page +
                                                "&per_page=100", token);
        //TODO Remove guards when done
        Thread.sleep(1000);
        return response;
    }

    public boolean isTokenLimitExceeded(String token) throws IOException {
        Response response = makeAPICall(Endpoints.LIMIT.getUrl(),token);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
            response.close();
            JsonObject search = bodyJson.get("resources").getAsJsonObject().get("search").getAsJsonObject();
            int remaining = search.get("remaining").getAsInt();
            return remaining <= 0;
        } else {
            throw new HttpResponseException(response.code(),"GitHub Server Error");
        }
    }

    public Response searchRepoLabels(String name, String token) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 labels used THEORETICALLY SHOULD NOT HAPPEN
        Response response = makeAPICall(generateLabelsURL(name) + "?page=1&per_page=100",token);
        Thread.sleep(2000);
        return response;
    }

    public Response searchRepoLanguages(String name, String token) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 languages used THEORETICALLY SHOULD NOT HAPPEN
        Response response = makeAPICall(generateLanguagesURL(name) + "?page=1&per_page=100",token);
        Thread.sleep(2000);
        return response;
    }

    private Response makeAPICall(String reqURL, String token) throws IOException {
        return client.newCall(generateRequest(reqURL,token)).execute();
    }

    private Request generateRequest(String reqURL, String token){
        return new Request.Builder()
                          .url(reqURL)
                          .addHeader("Authorization", "token " + token)
                          .addHeader("Accept", "application/vnd.github.v3+json")
                          .build();
    }

    private String generateRepoURL(String name){ return Endpoints.REPOS.getUrl() + "/" + name; }

    private String generateLabelsURL(String name){ return generateRepoURL(name) + "/labels"; }

    private String generateLanguagesURL(String name){ return generateRepoURL(name) + "/languages"; }
}
