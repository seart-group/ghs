package com.dabico.githubseapp.job;

import com.dabico.githubseapp.util.DateInterval;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.dabico.githubseapp.util.DateUtils.*;

@Service
public class CrawlProjectsJob {

    private OkHttpClient client = new OkHttpClient();
    private String clientSecret = "56583668e32b73702785a85900975d1ceccf15d5";
    private final Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);
    private List<DateInterval> requestQueue = new ArrayList<>();

    public void run(){
        requestQueue.add(new DateInterval(firstYearDay(2008),setEndDay(new Date())));

        do {
            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval){
        Request request = new Request.Builder()
                .url("https://api.github.com/search/repositories?q=language:Java" +
                     interval.getSearchURL() +
                     "&sort=stars&order=desc&page=1&per_page=100")
                .addHeader("Authorization", clientSecret)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();

        Call call = client.newCall(request);
        Response response;
        try {
            response = call.execute();
            if (response.isSuccessful()){
                String responseString = response.body().string();
                JsonObject bodyJson = JsonParser.parseString(responseString).getAsJsonObject();

                if (bodyJson.get("total_count").getAsInt() <= 1000){
                    JsonArray results = bodyJson.get("items").getAsJsonArray();
                    //store retrieved results

                    //do for
                    //iterate over the remaining pages
                    //for each page of results, store all the retrieved repos in the database
                } else {
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    requestQueue.add(newIntervals.getValue0());
                    requestQueue.add(newIntervals.getValue1());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
