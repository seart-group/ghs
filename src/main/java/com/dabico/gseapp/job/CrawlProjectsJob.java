package com.dabico.gseapp.job;

import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.repository.*;
import com.dabico.gseapp.util.DateInterval;
import com.google.gson.*;
import okhttp3.*;
import org.javatuples.Pair;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.dabico.gseapp.util.DateUtils.*;
import static com.dabico.gseapp.converter.GitRepoConverter.*;
import static com.google.gson.JsonParser.*;

@Service
public class CrawlProjectsJob {

    static final Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);

    private List<DateInterval> requestQueue = new ArrayList<>();
    private List<String> accessTokens = new ArrayList<>();
    private List<String> languages = new ArrayList<>();

    private int tokenOrdinal = -1;
    private String currentToken;

    private GitRepoRepository gitRepoRepository;
    private AccessTokenRepository accessTokenRepository;
    private SupportedLanguageRepository supportedLanguageRepository;

    @Autowired
    public CrawlProjectsJob(GitRepoRepository gitRepoRepository,
                            AccessTokenRepository accessTokenRepository,
                            SupportedLanguageRepository supportedLanguageRepository){
        this.gitRepoRepository = gitRepoRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.supportedLanguageRepository = supportedLanguageRepository;
        getLanguagesToMine();
        getAccessTokens();
        this.currentToken = getNewToken();
    }

    public void run() throws Exception {
        for(String language : languages){
            requestQueue.add(new DateInterval("2008-01-01T00:00:00..2009-01-01T00:00:00"));
            do {
                DateInterval first = requestQueue.remove(0);
                retrieveRepos(first,language);
            } while (!requestQueue.isEmpty());
        }
    }

    private void retrieveRepos(DateInterval interval, String language) throws Exception {
        logger.info("Crawling: "+language.toUpperCase()+" "+interval);
        logger.info("Token: " + this.currentToken);
        int page = 1;
        GitHubApiService gitHubApiService = new GitHubApiService();
        if (gitHubApiService.isTokenLimitExceeded(currentToken)){
            currentToken = getNewToken();
        }
        Response response = gitHubApiService.searchRepositories(language,interval,page,currentToken,false);
        ResponseBody responseBody = response.body();

        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
            int totalResults = bodyJson.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults/100.0);
            logger.info("Retrieved results: "+totalResults);
            if (totalResults <= 1000){
                JsonArray results = bodyJson.get("items").getAsJsonArray();
                response.close();
                logger.info("Adding: "+results.size()+" results");
                for (JsonElement element : results){
                    JsonObject repository = element.getAsJsonObject();
                    gitRepoRepository.save(jsonToGitRepo(repository));
                }

                if (totalPages > 1){
                    page++;
                    while (page <= totalPages){
                        if (gitHubApiService.isTokenLimitExceeded(currentToken)){
                            currentToken = getNewToken();
                        }
                        response = gitHubApiService.searchRepositories(language,interval,page,currentToken,false);
                        responseBody = response.body();
                        if (response.isSuccessful() && responseBody != null){
                            bodyJson = parseString(responseBody.string()).getAsJsonObject();
                            response.close();
                            results = bodyJson.get("items").getAsJsonArray();
                            logger.info("Adding: "+results.size()+" results");
                            for (JsonElement element : results){
                                JsonObject repository = element.getAsJsonObject();
                                gitRepoRepository.save(jsonToGitRepo(repository));
                            }
                            page++;
                        }
                        response.close();
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
        }
        response.close();
        logger.info(requestQueue.toString());
    }

    private void getLanguagesToMine(){
        //TODO Fix issue with flyway not initialising supported_language table properly
        //supportedLanguageRepository.findAll().forEach(language -> {
        //    languages.add(language.getLanguage());
        //});
        languages.add("Java");
    }

    private void getAccessTokens(){
        //TODO Fix issue with flyway not initialising access_token table properly
        //accessTokenRepository.findAll().forEach(accessToken -> {
        //    accessTokens.add(accessToken.getToken());
        //});
        accessTokens.add("faa5d7ae42793a29c360572fd519d5438d41962b");
    }

    private String getNewToken(){
        tokenOrdinal++;
        return accessTokens.get(tokenOrdinal % accessTokens.size());
    }
}
