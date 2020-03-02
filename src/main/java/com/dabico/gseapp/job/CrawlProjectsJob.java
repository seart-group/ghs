package com.dabico.gseapp.job;

import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.util.DateInterval;
import com.google.gson.*;
import okhttp3.*;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.dabico.gseapp.util.DateUtils.*;
import static com.dabico.gseapp.converter.GitRepoConverter.*;
import static com.google.gson.JsonParser.*;
import static java.time.LocalDate.*;

@Service
public class CrawlProjectsJob {

    static final Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);

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
            requestQueue.add(new DateInterval(firstDayOfYear(2008),lastDayOfYear(now().getYear())));
            do {
                DateInterval first = requestQueue.remove(0);
                retrieveRepos(first,language,requestQueue);
            } while (!requestQueue.isEmpty());
        });
    }

    private void retrieveRepos(DateInterval interval,String language,List<DateInterval> requestQueue){
        logger.info("Crawling: "+language.toUpperCase()+" "+interval);

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
                            } else if (response.code() == 403) {
                                // request limit exceeded, retry later
                                response.close();
                                requestQueue.add(interval);
                                // preemptively terminate loop
                                page = totalPages;
                            }
                            page++;
                        }
                    }
                } else {
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    if (newIntervals != null){
                        requestQueue.add(newIntervals.getValue0());
                        requestQueue.add(newIntervals.getValue1());
                    } else {
                        response.close();
                    }
                }
            } else if (response.code() == 403) {
                // request limit exceeded, retry later
                response.close();
                requestQueue.add(interval);
            }
        } catch (IOException e) {
            // Something went wrong when reading the response
            requestQueue.add(interval);
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
