package com.dabico.gseapp.job;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.AccessTokenRepository;
import com.dabico.gseapp.repository.SupportedLanguageRepository;
import com.dabico.gseapp.service.ApplicationPropertyService;
import com.dabico.gseapp.service.CrawlJobService;
import com.dabico.gseapp.service.GitRepoService;
import com.dabico.gseapp.util.interval.DateInterval;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.client.HttpResponseException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    static Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);

    @NonFinal
    static Long defaultRetryPeriod = 900000L;

    List<DateInterval> requestQueue = new ArrayList<>();
    List<String> accessTokens = new ArrayList<>();
    List<String> languages = new ArrayList<>();

    @NonFinal
    int tokenOrdinal;
    @NonFinal
    String currentToken;

    AccessTokenRepository accessTokenRepository;
    SupportedLanguageRepository supportedLanguageRepository;

    GitRepoConverter gitRepoConverter;

    GitHubApiService gitHubApiService;
    GitRepoService gitRepoService;
    CrawlJobService crawlJobService;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public CrawlProjectsJob(AccessTokenRepository accessTokenRepository,
                            SupportedLanguageRepository supportedLanguageRepository,
                            GitRepoConverter gitRepoConverter,
                            GitHubApiService gitHubApiService,
                            GitRepoService gitRepoService,
                            CrawlJobService crawlJobService,
                            ApplicationPropertyService applicationPropertyService){
        this.accessTokenRepository = accessTokenRepository;
        this.supportedLanguageRepository = supportedLanguageRepository;
        this.gitRepoConverter = gitRepoConverter;
        this.gitHubApiService = gitHubApiService;
        this.gitRepoService = gitRepoService;
        this.crawlJobService = crawlJobService;
        this.applicationPropertyService = applicationPropertyService;
    }

    public void run() throws IOException,InterruptedException {
        reset();
        Date endDate = Date.from(Instant.now().minus(Duration.ofHours(2)));
        for (String language : languages){
            Date startDate = crawlJobService.getCrawlDateByLanguage(language);
            DateInterval interval;
            if (startDate != null){
                assert startDate.before(endDate);
                interval = DateInterval.builder().start(startDate).end(endDate).build();
                crawlCreatedRepos(interval,language);
                crawlUpdatedRepos(interval,language);
            } else {
                interval = DateInterval.builder().start(applicationPropertyService.getStartDate()).end(endDate).build();
                crawlCreatedRepos(interval,language);
            }
        }
    }

    private void crawlCreatedRepos(DateInterval interval, String language) throws IOException,InterruptedException {
        logger.info("Created: "+language.toUpperCase()+" "+interval);
        logger.info("Token: " + this.currentToken);
        crawlRepos(interval,language,false);
        logger.info("Created interval crawl complete!");
    }

    private void crawlUpdatedRepos(DateInterval interval, String language) throws IOException,InterruptedException {
        logger.info("Updated: "+language.toUpperCase()+" "+interval);
        logger.info("Token: " + this.currentToken);
        crawlRepos(interval,language,true);
        logger.info("Updated interval crawl complete!");
    }

    private void crawlRepos(DateInterval interval, String language, Boolean crawl_updated_repos)
            throws IOException,InterruptedException {
        requestQueue.add(interval);
        do {
            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first,language,crawl_updated_repos);
            if (!requestQueue.isEmpty()) {
                logger.info("Next Crawl Intervals:");
                logger.info(requestQueue.toString());
            }
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval, String language, Boolean crawl_updated_repos)
            throws IOException,InterruptedException {
        int page = 1;
        replaceTokenIfExpired();
        Response response = gitHubApiService.searchRepositories(language, interval, page, currentToken, crawl_updated_repos);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = JsonParser.parseString(responseBody.string()).getAsJsonObject();
            int totalResults = bodyJson.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults/100.0);
            logger.info("Retrieved results: "+totalResults);
            if (totalResults <= 1000){
                JsonArray results = bodyJson.get("items").getAsJsonArray();
                response.close();
                saveRetrievedRepos(results,language);
                retrieveRemainingRepos(interval, language, crawl_updated_repos, results, totalPages);
                crawlJobService.updateCrawlDateForLanguage(language,interval.getEnd());
            } else {
                Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                if (newIntervals != null){
                    requestQueue.add(0,newIntervals.getValue1());
                    requestQueue.add(0,newIntervals.getValue0());
                }
                response.close();
            }
        } else if (response.code() > 499){
            logger.error("Error retrieving repositories.");
            logger.error("Server Error Encountered: " + response.code());
            Thread.sleep(defaultRetryPeriod);
            logger.error("Retrying...");
            retrieveRepos(interval, language, crawl_updated_repos);
        }
        response.close();
    }

    private void retrieveRemainingRepos(DateInterval interval, String language, Boolean crawl_updated_repos,
                                        JsonArray results, int totalPages) throws IOException,InterruptedException {
        if (totalPages > 1){
            int page = 2;
            while (page <= totalPages){
                replaceTokenIfExpired();
                Response response = gitHubApiService.searchRepositories(language, interval, page, currentToken, crawl_updated_repos);
                ResponseBody responseBody = response.body();
                if (response.isSuccessful() && responseBody != null){
                    JsonObject bodyJson = JsonParser.parseString(responseBody.string()).getAsJsonObject();
                    response.close();
                    results = bodyJson.get("items").getAsJsonArray();
                    saveRetrievedRepos(results,language);
                    page++;
                } else if (response.code() > 499){
                    logger.error("Error retrieving repositories at page: " + page);
                    logger.error("Server Error Encountered: " + response.code());
                    Thread.sleep(defaultRetryPeriod);
                    logger.error("Retrying...");
                    retrieveRemainingRepos(interval, language, crawl_updated_repos, results, totalPages);
                }
                response.close();
            }
        }
    }

    private void saveRetrievedRepos(JsonArray results, String language) throws IOException,InterruptedException {
        logger.info("Adding: "+results.size()+" repositories.");
        for (JsonElement element : results){
            JsonObject repoJson = element.getAsJsonObject();
            GitRepo repo = gitRepoConverter.jsonToGitRepo(repoJson,language);
            if (repo != null){
                repo = gitRepoService.createOrUpdateRepo(repo);
                retrieveRepoLabels(repo);
                retrieveRepoLanguages(repo);
            }
        }
    }

    private void retrieveRepoLabels(GitRepo repo) throws IOException,InterruptedException {
        List<GitRepoLabel> repo_labels = new ArrayList<>();
        Response response = gitHubApiService.searchRepoLabels(repo.getName(),currentToken);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonArray results = JsonParser.parseString(responseBody.string()).getAsJsonArray();
            logger.info("Adding: "+results.size()+" labels.");
            results.forEach(result -> repo_labels.add(GitRepoLabel.builder()
                                                 .repo(repo)
                                                 .label(result.getAsJsonObject().get("name").getAsString())
                                                 .build())
            );
            gitRepoService.createUpdateLabels(repo,repo_labels);
        } else if (response.code() > 499){
            logger.error("Error retrieving labels.");
            logger.error("Server Error Encountered: " + response.code());
            Thread.sleep(defaultRetryPeriod);
            logger.error("Retrying...");
            retrieveRepoLabels(repo);
        }
        response.close();
    }

    private void retrieveRepoLanguages(GitRepo repo) throws IOException,InterruptedException {
        List<GitRepoLanguage> repo_languages = new ArrayList<>();
        Response response = gitHubApiService.searchRepoLanguages(repo.getName(),currentToken);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject result = JsonParser.parseString(responseBody.string()).getAsJsonObject();
            Set<String> keySet = result.keySet();
            logger.info("Adding: "+keySet.size()+" languages.");
            keySet.forEach(key -> repo_languages.add(GitRepoLanguage.builder()
                                                                    .repo(repo)
                                                                    .language(key)
                                                                    .sizeOfCode(result.get(key).getAsLong())
                                                                    .build())
            );
            gitRepoService.createUpdateLanguages(repo,repo_languages);
        } else if (response.code() > 499){
            logger.error("Error retrieving languages.");
            logger.error("Server Error Encountered: " + response.code());
            Thread.sleep(defaultRetryPeriod);
            logger.error("Retrying...");
            retrieveRepoLanguages(repo);
        }
        response.close();
    }

    private void reset(){
        getLanguagesToMine();
        getAccessTokens();
        this.tokenOrdinal = -1;
        this.currentToken = getNewToken();
    }

    private void getLanguagesToMine(){
        supportedLanguageRepository.findAll().forEach(language -> languages.add(language.getName()));
    }

    private void getAccessTokens(){
        accessTokenRepository.findAll().forEach(accessToken -> accessTokens.add(accessToken.getValue()));
    }

    private void replaceTokenIfExpired() throws IOException,InterruptedException {
        try {
            if (gitHubApiService.isTokenLimitExceeded(currentToken)){
                currentToken = getNewToken();
            }
        } catch (HttpResponseException ex) {
            logger.error("Error communicating with GitHub.");
            logger.error("Server Error Encountered: " + ex.getStatusCode());
            Thread.sleep(defaultRetryPeriod);
            logger.error("Retrying...");
        }
    }

    private String getNewToken(){
        tokenOrdinal = (tokenOrdinal + 1) % accessTokens.size();
        return accessTokens.get(tokenOrdinal);
    }
}
