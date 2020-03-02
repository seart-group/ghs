package com.dabico.gseapp.job;

import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.util.DateInterval;
import com.google.gson.*;
import okhttp3.*;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.dabico.gseapp.util.DateUtils.*;
import static com.dabico.gseapp.converter.GitRepoConverter.*;
import static com.google.gson.JsonParser.*;

@Service
public class CrawlProjectsJob {

    private GitRepoRepository gitRepoRepository;

    @Autowired
    public CrawlProjectsJob(GitRepoRepository gitRepoRepository){
        this.gitRepoRepository = gitRepoRepository;
    }

    //TODO Make sure service is not parallelised
    public void run(){
        Set<String> minedLanguages = getLanguagesToMine();
        minedLanguages.forEach(language -> {
            List<DateInterval> requestQueue = new ArrayList<>();
            requestQueue.add(new DateInterval(firstDayOfYear(2008),lastDayOfYear(2020)));
            do {
                DateInterval first = requestQueue.remove(0);
                retrieveRepos(first,language,requestQueue);
            } while (!requestQueue.isEmpty());
        });
    }

    private void retrieveRepos(DateInterval interval,String language,List<DateInterval> requestQueue){
        int page = 1;
        GitHubApiService gitHubApiService = new GitHubApiService();

        try {
            Response response = gitHubApiService.gitHubSearchRepositories(language,interval,page);
            ResponseBody responseBody = response.body();

            if (response.isSuccessful() && responseBody != null){
                JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
                int totalResults = bodyJson.get("total_count").getAsInt();
                int totalPages = (int) Math.ceil(totalResults/100.0);

                if (totalResults <= 1000){
                    JsonArray results = bodyJson.get("items").getAsJsonArray();
                    results.forEach(element -> {
                        JsonObject repository = element.getAsJsonObject();
                        gitRepoRepository.save(jsonToGitRepo(repository));
                    });
                    responseBody.close();

                    //TODO iterate over the remaining pages
                    //for each page of results, store all the retrieved repos in the database
                    if (totalPages > 1){
                        page++;
                        while (page <= totalPages){
                            response = gitHubApiService.gitHubSearchRepositories(language,interval,page);
                            responseBody = response.body();
                            if (response.isSuccessful() && responseBody != null){
                                bodyJson = parseString(responseBody.string()).getAsJsonObject();
                                results = bodyJson.get("items").getAsJsonArray();
                                results.forEach(element -> {
                                    JsonObject repository = element.getAsJsonObject();
                                    gitRepoRepository.save(jsonToGitRepo(repository));
                                });
                                responseBody.close();
                            } //TODO What should you do in case you don't get any results?
                            page++;
                        }
                    }
                } else {
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    if (newIntervals != null){
                        requestQueue.add(newIntervals.getValue0());
                        requestQueue.add(newIntervals.getValue1());
                    }
                }
            } //TODO What should you do in case you don't get any results?
        } catch (IOException e) {
            //TODO Better exception handling in case call fails
            e.printStackTrace();
        }
    }

    private Set<String> getLanguagesToMine(){
        Set<String> languages = new HashSet<>();
        languages.add("Java");
//        languages.add("Kotlin");
//        languages.add("C");
//        languages.add("CPP");
//        languages.add("Python");
//        languages.add("Javascript");
//        languages.add("Typescript");
        return languages;
    }
}
