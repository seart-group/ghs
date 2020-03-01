package com.dabico.gseapp.job;

import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.util.DateInterval;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.dabico.gseapp.util.DateUtils.*;

@Service
public class CrawlProjectsJob {

    private OkHttpClient client = new OkHttpClient();
    private List<DateInterval> requestQueue = new ArrayList<>();

    private GitRepoRepository gitRepoRepository;

    public void run(){
        requestQueue.add(new DateInterval(firstDayOfYear(2008),setEndDay(new Date())));
        do {
            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval){
        int page = 1;
        Request request = new Request.Builder()
                .url("https://api.github.com/search/repositories?q=language:Java" +
                     interval.toString() +
                     "+fork:true&page=" + page +
                     "&per_page=100")
                .addHeader("Authorization", "56583668e32b73702785a85900975d1ceccf15d5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();

        Call call = client.newCall(request);
        Response response;
        try {
            response = call.execute();
            ResponseBody responseBody = response.body();

            if (response.isSuccessful() && responseBody != null){
                String responseString = responseBody.string();
                JsonObject bodyJson = JsonParser.parseString(responseString).getAsJsonObject();
                int totalResults = bodyJson.get("total_count").getAsInt();

                if (totalResults <= 1000){
                    JsonArray results = bodyJson.get("items").getAsJsonArray();
                    results.forEach((element) -> {
                        //TODO For each project do a scrape of specific project pages
                        JsonObject repository = element.getAsJsonObject();
                        String repositoryURL = repository.get("html_url").getAsString();
                        //TODO Configure builder
                        GitRepo newRepo = GitRepo.builder()
                                                 .isFork(repository.get("fork").getAsBoolean())
                                                 //commits
                                                 //branches
                                                 .defaultBranch(repository.get("default_branch").getAsString())
                                                 //releases
                                                 //contributors
                                                 .license(repository.get("license").getAsJsonObject().get("name").getAsString())
                                                 //watchers
                                                 .stargazers(repository.get("stargazers_count").getAsLong())
                                                 .forks(repository.get("forks_count").getAsLong())
                                                 .size(repository.get("size").getAsLong())
                                                 .mainLanguage(repository.get("language").getAsString())
                                                 //total issues
                                                 //open issues
                                                 //total pull requests
                                                 //open pull requests
                                                 //last commit
                                                 //last commit sha
                                                 .hasWiki(repository.get("has_wiki").getAsBoolean())
                                                 .isArchived(repository.get("archived").getAsBoolean())
                                                 .build();
                        gitRepoRepository.save(newRepo);
                    });

                    //TODO iterate over the remaining pages
                    //for each page of results, store all the retrieved repos in the database
                } else {
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    requestQueue.add(newIntervals.getValue0());
                    requestQueue.add(newIntervals.getValue1());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
