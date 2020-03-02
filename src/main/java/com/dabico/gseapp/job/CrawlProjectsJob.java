package com.dabico.gseapp.job;

import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.github.GitHubPageCrawlerService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.util.DateInterval;
import com.google.gson.*;
import okhttp3.*;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.dabico.gseapp.util.DateUtils.*;

@Service
public class CrawlProjectsJob {

    private GitHubApiService gitHubApiService;
    private List<DateInterval> requestQueue = new ArrayList<>();

    private GitRepoRepository gitRepoRepository;

    public void run(){
        this.gitHubApiService = new GitHubApiService();
        requestQueue.add(new DateInterval(firstDayOfYear(2008),setEndDay(new Date())));
        do {
            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval){
        int page = 1;

        try {
            Response response = gitHubApiService.gitHubSearchRepositories("Java",interval,page);
            ResponseBody responseBody = response.body();

            if (response.isSuccessful() && responseBody != null){
                String responseString = responseBody.string();
                JsonObject bodyJson = JsonParser.parseString(responseString).getAsJsonObject();
                int totalResults = bodyJson.get("total_count").getAsInt();
                int totalPages = (int) Math.ceil(totalResults/100.0);

                if (totalResults <= 1000){
                    JsonArray results = bodyJson.get("items").getAsJsonArray();
                    results.forEach(element -> {
                        JsonObject repository = element.getAsJsonObject();
                        String repositoryURL = repository.get("html_url").getAsString();
                        //TODO Pass Response objects to some kind of respective parser
                        GitHubPageCrawlerService crawlerService = new GitHubPageCrawlerService(repositoryURL);
                        //TODO Configure builder
                        GitRepo newRepo = GitRepo.builder()
                                                 .isFork(repository.get("fork").getAsBoolean())
                                                 //commits - main project page
                                                 //branches - main project page
                                                 .defaultBranch(repository.get("default_branch").getAsString())
                                                 //releases - main project page
                                                 //contributors - main project page
                                                 .license(repository.get("license").getAsJsonObject().get("name").getAsString())
                                                 //watchers - main project page
                                                 .stargazers(repository.get("stargazers_count").getAsLong())
                                                 .forks(repository.get("forks_count").getAsLong())
                                                 .size(repository.get("size").getAsLong())
                                                 .mainLanguage(repository.get("language").getAsString())
                                                 //total issues - issues page
                                                 //open issues - issues page
                                                 //total pull requests - pulls page
                                                 //open pull requests - pulls page
                                                 //last commit - commits page
                                                 //last commit sha - commits page
                                                 .hasWiki(repository.get("has_wiki").getAsBoolean())
                                                 .isArchived(repository.get("archived").getAsBoolean())
                                                 .build();
                        gitRepoRepository.save(newRepo);
                    });

                    //TODO iterate over the remaining pages
                    //for each page of results, store all the retrieved repos in the database
                    if (totalPages > 1){
                        page++;
                        while (page <= totalPages){
                            response = gitHubApiService.gitHubSearchRepositories("Java",interval,page);
                            responseBody = response.body();
                            if (response.isSuccessful() && responseBody != null){
                                responseString = responseBody.string();
                                bodyJson = JsonParser.parseString(responseString).getAsJsonObject();
                                results = bodyJson.get("items").getAsJsonArray();
                                results.forEach(element -> {
                                    //TODO Extract duplicate fragment into separate method/class
                                    JsonObject repository = element.getAsJsonObject();
                                    String repositoryURL = repository.get("html_url").getAsString();
                                    GitHubPageCrawlerService crawlerService = new GitHubPageCrawlerService(repositoryURL);
                                    GitRepo newRepo = GitRepo.builder()
                                            .isFork(repository.get("fork").getAsBoolean())
                                            .defaultBranch(repository.get("default_branch").getAsString())
                                            .license(repository.get("license").getAsJsonObject().get("name").getAsString())
                                            .stargazers(repository.get("stargazers_count").getAsLong())
                                            .forks(repository.get("forks_count").getAsLong())
                                            .size(repository.get("size").getAsLong())
                                            .mainLanguage(repository.get("language").getAsString())
                                            .hasWiki(repository.get("has_wiki").getAsBoolean())
                                            .isArchived(repository.get("archived").getAsBoolean())
                                            .build();
                                    gitRepoRepository.save(newRepo);
                                });
                            } else {
                                //TODO What should you do in case you don't get any results?
                            }
                            page++;
                        }
                    }
                } else {
                    Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                    requestQueue.add(newIntervals.getValue0());
                    requestQueue.add(newIntervals.getValue1());
                }
            } else {
                //TODO What should you do in case you don't get any results?
            }
        } catch (IOException e) {
            //TODO Better exception handling in case call fails
            e.printStackTrace();
        }
    }
}
