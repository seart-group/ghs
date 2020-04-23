package com.dabico.gseapp.job;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.github.GitHubApiService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.*;
import com.dabico.gseapp.service.ApplicationPropertyService;
import com.dabico.gseapp.service.CrawlJobService;
import com.dabico.gseapp.service.GitRepoService;
import com.dabico.gseapp.util.interval.DateInterval;
import com.google.gson.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.*;
import org.javatuples.Pair;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.google.gson.JsonParser.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlProjectsJob {

    static final Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);

    List<DateInterval> requestQueue = new ArrayList<>();
    List<String> accessTokens = new ArrayList<>();
    List<String> languages = new ArrayList<>();

    int tokenOrdinal;
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

    public void run() throws Exception {
        reset();
        Date startDate = Date.from(Instant.now().minus(Duration.ofHours(2)));
        for (String language : languages){
            Date limit = crawlJobService.getCrawlDateByLanguage(language);
            DateInterval interval;
            if (limit != null){
                assert limit.before(startDate);
                interval = new DateInterval(limit,startDate);
                create(interval,language);
                update(interval,language);
            } else {
                interval = new DateInterval(applicationPropertyService.getStartDate(),startDate);
                create(interval,language);
            }
        }
    }

    private void create(DateInterval interval, String language) throws Exception {
        logger.info("Created: "+language.toUpperCase()+" "+interval);
        logger.info("Token: " + this.currentToken);
        crawl(interval,language,false);
        logger.info("Create Complete!");
    }

    private void update(DateInterval interval, String language) throws Exception {
        logger.info("Updated: "+language.toUpperCase()+" "+interval);
        logger.info("Token: " + this.currentToken);
        crawl(interval,language,true);
        logger.info("Update Complete!");
    }

    private void crawl(DateInterval interval, String language, Boolean mode) throws Exception {
        requestQueue.add(interval);
        do {
            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first,language,mode);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval, String language, Boolean update) throws Exception {
        int page = 1;
        replaceTokenIfExpired();
        Response response = gitHubApiService.searchRepositories(language,interval,page,currentToken,update);
        ResponseBody responseBody = response.body();

        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = parseString(responseBody.string()).getAsJsonObject();
            int totalResults = bodyJson.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults/100.0);
            logger.info("Retrieved results: "+totalResults);
            if (totalResults <= 1000){
                JsonArray results = bodyJson.get("items").getAsJsonArray();
                response.close();
                saveRetrievedRepos(results,language);

                if (totalPages > 1){
                    page++;
                    while (page <= totalPages){
                        replaceTokenIfExpired();
                        response = gitHubApiService.searchRepositories(language,interval,page,currentToken,update);
                        responseBody = response.body();
                        if (response.isSuccessful() && responseBody != null){
                            bodyJson = parseString(responseBody.string()).getAsJsonObject();
                            response.close();
                            results = bodyJson.get("items").getAsJsonArray();
                            saveRetrievedRepos(results,language);
                            page++;
                        }
                        response.close();
                    }
                }
                crawlJobService.updateCrawlDateForLanguage(language,interval.getEnd());
            } else {
                Pair<DateInterval,DateInterval> newIntervals = interval.splitInterval();
                if (newIntervals != null){
                    requestQueue.add(newIntervals.getValue0());
                    requestQueue.add(newIntervals.getValue1());
                }
                response.close();
            }
        }
        response.close();
        if (!requestQueue.isEmpty()) {
            logger.info("Next Crawl Intervals:");
            logger.info(requestQueue.toString());
        }
    }

    private void saveRetrievedRepos(JsonArray results, String language) throws Exception {
        logger.info("Adding: "+results.size()+" results");
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

    private void retrieveRepoLabels(GitRepo repo) throws Exception {
        List<GitRepoLabel> repo_labels = new ArrayList<>();
        Response response = gitHubApiService.searchRepoLabels(repo.getName(),currentToken);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonArray results = parseString(responseBody.string()).getAsJsonArray();
            results.forEach(result ->
                    repo_labels.add(GitRepoLabel.builder()
                                                .repo(repo)
                                                .label(result.getAsJsonObject().get("name").getAsString())
                                                .build())
            );
        }
        gitRepoService.createUpdateLabels(repo,repo_labels);
        response.close();
    }

    private void retrieveRepoLanguages(GitRepo repo) throws Exception {
        List<GitRepoLanguage> repo_languages = new ArrayList<>();
        Response response = gitHubApiService.searchRepoLanguages(repo.getName(),currentToken);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject result = parseString(responseBody.string()).getAsJsonObject();
            Set<String> keySet = result .keySet();
            keySet.forEach(key -> repo_languages.add(GitRepoLanguage.builder()
                                                                    .repo(repo)
                                                                    .language(key)
                                                                    .sizeOfCode(result.get(key).getAsLong())
                                                                    .build())
            );
        }
        gitRepoService.createUpdateLanguages(repo,repo_languages);
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

    private void replaceTokenIfExpired() throws Exception {
        if (gitHubApiService.isTokenLimitExceeded(currentToken)){
            currentToken = getNewToken();
        }
    }

    private String getNewToken(){
        tokenOrdinal = (tokenOrdinal + 1) % accessTokens.size();
        return accessTokens.get(tokenOrdinal);
    }
}
