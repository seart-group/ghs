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
    private List<DateInterval> requestQueue = new ArrayList<>();
    private List<DateInterval> retryQueue = new ArrayList<>();

    private GitRepoRepository gitRepoRepository;

    @Autowired
    public CrawlProjectsJob(GitRepoRepository gitRepoRepository){
        this.gitRepoRepository = gitRepoRepository;
    }

    //TODO Make sure service is not parallelised
    public void run(){
        Set<String> minedLanguages = getLanguagesToMine();
        minedLanguages.forEach(language -> {
            requestQueue.add(new DateInterval(firstDayOfYear(2008),new Date()));
            do {
                DateInterval first = requestQueue.remove(0);
                retrieveRepos(first,language);
            } while (!requestQueue.isEmpty());
        });
    }

    private void retrieveRepos(DateInterval interval, String language){
        logger.info("Crawling: "+language.toUpperCase()+" "+interval);

        int page = 1;
        GitHubApiService gitHubApiService = new GitHubApiService();

        try {
            Response response = gitHubApiService.gitHubSearchRepositories(language,interval,page);
            //TODO Find a better way to slow down the requests
            Thread.sleep(7500);
            ResponseBody responseBody = response.body();

            if (response.isSuccessful() && responseBody != null){
                JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
                int totalResults = bodyJson.get("total_count").getAsInt();
                int totalPages = (int) Math.ceil(totalResults/100.0);
                logger.info("Retrieved results: "+totalResults);
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
                                // request limit exceeded, preemptively terminate loop and retry later
                                response.close();
                                retryQueue.add(interval);
                                page = totalPages;
                            }
                            page++;
                        }
                    }
                } else {
                    //split search interval
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    if (newIntervals != null){
                        requestQueue.add(newIntervals.getValue0());
                        requestQueue.add(newIntervals.getValue1());
                    }
                    response.close();
                }
            } else if (response.code() == 403) {
                // request limit exceeded, retry later
                response.close();
                retryQueue.add(interval);
            }
        } catch (IOException | InterruptedException e) {
            // Something went wrong when reading the response or thread was interrupted
            retryQueue.add(interval);
            e.printStackTrace();
        } finally {
            logger.info(requestQueue.toString());
            logger.info(retryQueue.toString());
        }
    }

    private Set<String> getLanguagesToMine(){
        //TODO configure access of git.toMine instead of this
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
