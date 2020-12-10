package com.dabico.gseapp.job;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.github_service.GitHubApiService;
import com.dabico.gseapp.github_service.RepoHtmlPageExtraInfo;
import com.dabico.gseapp.github_service.RepoHtmlPageParserService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.AccessTokenRepository;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.repository.SupportedLanguageRepository;
import com.dabico.gseapp.db_access_service.ApplicationPropertyService;
import com.dabico.gseapp.db_access_service.CrawlJobService;
import com.dabico.gseapp.db_access_service.GitRepoService;
import com.dabico.gseapp.util.DateUtils;
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
import org.jsoup.HttpStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    static Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);

    @NonFinal
    static Long defaultRetryPeriod = 900000L;

    @NonFinal
    public boolean running = false;
    List<DateInterval> requestQueue = new ArrayList<>();
    List<String> accessTokens = new ArrayList<>();
    List<String> languages = new ArrayList<>();

    @NonFinal
    int tokenOrdinal;
    @NonFinal
    String currentToken;

    AccessTokenRepository accessTokenRepository;
    SupportedLanguageRepository supportedLanguageRepository;
    RepoHtmlPageParserService repoHtmlPageParserService;
    GitRepoRepository gitRepoRepository;

    GitHubApiService gitHubApiService;
    GitRepoService gitRepoService;
    CrawlJobService crawlJobService;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public CrawlProjectsJob(AccessTokenRepository accessTokenRepository,
                            SupportedLanguageRepository supportedLanguageRepository,
                            RepoHtmlPageParserService repoHtmlPageParserService,
                            GitHubApiService gitHubApiService,
                            GitRepoService gitRepoService,
                            CrawlJobService crawlJobService,
                            ApplicationPropertyService applicationPropertyService,
                            GitRepoRepository gitRepoRepository){
        this.accessTokenRepository = accessTokenRepository;
        this.supportedLanguageRepository = supportedLanguageRepository;
        this.repoHtmlPageParserService = repoHtmlPageParserService;
        this.gitHubApiService = gitHubApiService;
        this.gitRepoService = gitRepoService;
        this.crawlJobService = crawlJobService;
        this.applicationPropertyService = applicationPropertyService;
        this.gitRepoRepository = gitRepoRepository;
    }

    public void run() throws IOException,InterruptedException {
        this.running = true;
        reset();
        logger.info("New Crawling for all languages: "+languages);
        Date endDate = Date.from(Instant.now().minus(Duration.ofHours(2)));
        for (String language : languages){
            Date startDate = crawlJobService.getCrawlDateByLanguage(language);
            DateInterval interval;

            if (startDate != null){
                assert startDate.before(endDate);
                interval = DateInterval.builder().start(startDate).end(endDate).build();
//                crawlCreatedRepos(interval,language);
                crawlUpdatedRepos(interval,language);
            } else {
                interval = DateInterval.builder().start(applicationPropertyService.getStartDate()).end(endDate).build();
//                crawlCreatedRepos(interval,language);
                crawlUpdatedRepos(interval,language);
            }
        }
        this.running = false;
    }

    private void crawlCreatedRepos(DateInterval interval, String language) throws IOException,InterruptedException {
        logger.info("Crawling "+language.toUpperCase()+" repositories CREATED: "+interval);
        crawlRepos(interval, language,false);
        logger.info("Crawling "+language.toUpperCase()+" repositories CREATED: "+interval+ " [FINISHED]");
    }

    private void crawlUpdatedRepos(DateInterval interval, String language) throws IOException,InterruptedException {
        logger.info("Crawling "+language.toUpperCase()+" repositories UPDATED: "+interval);
        crawlRepos(interval, language,true);
        logger.info("Crawling "+language.toUpperCase()+" repositories updated: "+interval+ " [FINISHED]");
    }

    private void crawlRepos(DateInterval interval, String language, Boolean crawl_updated_repos)
            throws IOException,InterruptedException
    {
        requestQueue.add(interval);
        do {
            logger.info("Next Crawl Intervals: "+requestQueue.toString());

            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first, language, crawl_updated_repos);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval, String language, Boolean crawl_updated_repos)
            throws IOException,InterruptedException
    {
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
                saveRetrievedRepos(results, language, 1, totalResults);
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
                    int totalResults = bodyJson.get("total_count").getAsInt();
                    results = bodyJson.get("items").getAsJsonArray();
                    saveRetrievedRepos(results, language, (page-1)*100+1, totalResults);
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

    /**
     * Given JSON info of 100 repos, store them in DB
     */
    private void saveRetrievedRepos(JsonArray results, String language, int repo_num_start, int repo_num_total) throws IOException,InterruptedException {
        logger.info("Adding: "+results.size()+" repositories ("+repo_num_start+"-"+(repo_num_start+results.size()-1)+" | total: "+repo_num_total+")");
        for (JsonElement element : results){
            JsonObject repoJson = element.getAsJsonObject();

            String repoFullName = repoJson.get("full_name").getAsString().toLowerCase();
            Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repoFullName);

            if(!opt.isPresent())
                logger.info(repo_num_start+"/"+repo_num_total+" saving new repo: "+repoFullName);
            else
                logger.info(repo_num_start+"/"+repo_num_total+" updating repo: "+repoFullName);

            repo_num_start++;

            // Optimization thing
            if(opt.isPresent())
            {
                GitRepo existingRepInfo = opt.get();
                Date existing_updatedAt = existingRepInfo.getUpdatedAt();
                Date existing_pushedAt = existingRepInfo.getPushedAt();

                Date repo_updatedAt = DateUtils.fromGitDateString(repoJson.get("updated_at").getAsString());
                Date repo_pushedAt = DateUtils.fromGitDateString(repoJson.get("pushed_at").getAsString());

//                boolean incompleteMinedInfo = existingRepInfo.getContributors() == null;

                if(/*incompleteMinedInfo==false &&*/ existing_updatedAt.compareTo(repo_updatedAt)==0 && existing_pushedAt.compareTo(repo_pushedAt)==0) {
                    logger.info("\tSKIPPED. We already have the latest info up to "+existing_updatedAt+"(updated)  "+existing_pushedAt+"(pushed)");
                    continue; // we already have the latest info for this repo
                }
            }

            if(repoJson.get("language").isJsonNull())
                repoJson.addProperty("language", language); // This can happen. Example Repo: "aquynh/iVM"

            GitRepo repo = createGitRepoRowObjectFromGitHubAPIResultJson(repoJson);
            repo = gitRepoService.createOrUpdateRepo(repo);
            logger.info("\tBasic information saved (repo Table).");
            retrieveRepoLabels(repo);
            retrieveRepoLanguages(repo);
        }
    }

    private GitRepo createGitRepoRowObjectFromGitHubAPIResultJson(JsonObject repoJson) throws IOException, InterruptedException {
        GitRepo.GitRepoBuilder gitRepoBuilder = GitRepo.builder();

        JsonElement license = repoJson.get("license");
        JsonElement homepage = repoJson.get("homepage");

        gitRepoBuilder.name(repoJson.get("full_name").getAsString().toLowerCase());
        gitRepoBuilder.isFork(repoJson.get("fork").getAsBoolean());
        gitRepoBuilder.defaultBranch(repoJson.get("default_branch").getAsString());
        gitRepoBuilder.license((license.isJsonNull()) ? null : license.getAsJsonObject()
                        .get("name")
                        .getAsString()
                        .replaceAll("\"", ""));
        gitRepoBuilder.stargazers(repoJson.get("stargazers_count").getAsLong());
        gitRepoBuilder.forks(repoJson.get("forks_count").getAsLong());
        gitRepoBuilder.size(repoJson.get("size").getAsLong());
        gitRepoBuilder.createdAt(DateUtils.fromGitDateString(repoJson.get("created_at").getAsString()));
        gitRepoBuilder.pushedAt(DateUtils.fromGitDateString(repoJson.get("pushed_at").getAsString()));
        gitRepoBuilder.updatedAt(DateUtils.fromGitDateString(repoJson.get("updated_at").getAsString()));
        gitRepoBuilder.homepage(homepage.isJsonNull() ? null : homepage.getAsString());
        gitRepoBuilder.mainLanguage(repoJson.get("language").getAsString());
        gitRepoBuilder.hasWiki(repoJson.get("has_wiki").getAsBoolean());
        gitRepoBuilder.isArchived(repoJson.get("archived").getAsBoolean());

        // #FutureWork: block below which is time-consuming can be done later.
        RepoHtmlPageExtraInfo extraMinedInfo = mineExtraInfoFromRepHTMLPage(repoJson);
        if(extraMinedInfo!=null)
        {
            gitRepoBuilder.commits(extraMinedInfo.getCommits());
            gitRepoBuilder.branches(extraMinedInfo.getBranches());
            gitRepoBuilder.releases(extraMinedInfo.getReleases());
            gitRepoBuilder.contributors(extraMinedInfo.getContributors());
            gitRepoBuilder.watchers(extraMinedInfo.getWatchers());
            gitRepoBuilder.totalIssues(extraMinedInfo.getTotalIssues());
            gitRepoBuilder.openIssues(extraMinedInfo.getOpenIssues());
            gitRepoBuilder.totalPullRequests(extraMinedInfo.getTotalPullRequests());
            gitRepoBuilder.openPullRequests(extraMinedInfo.getOpenPullRequests());
            gitRepoBuilder.lastCommit(extraMinedInfo.getLastCommit());
            gitRepoBuilder.lastCommitSHA(extraMinedInfo.getLastCommitSHA());
        }
        else
        {
            logger.warn("\tFailed to mine extra info: "+repoJson.get("full_name").getAsString());
        }

        return gitRepoBuilder.build();
    }

    /**
     * #FutureWork: block below which is time-consuming can be done later.
     */
    private RepoHtmlPageExtraInfo mineExtraInfoFromRepHTMLPage(JsonObject json) throws IOException,InterruptedException {
        String repositoryURL = json.get("html_url").getAsString();

        RepoHtmlPageExtraInfo extraMinedInfo = null;

        try {
            extraMinedInfo = repoHtmlPageParserService.mine(repositoryURL);
        } catch (HttpStatusException ex) {
            int code = ex.getStatusCode();
            logger.error(ex.getMessage()+": "+ex.getUrl());
            logger.error("Status: "+code);
            if (code == 404){
                logger.error("This repository no longer exists");
                return null;
            } else if (code == 429){
                logger.error("Retrying in 5min (due to error: 429, too many requests)");
                Thread.sleep(300000);
                return mineExtraInfoFromRepHTMLPage(json);
            }
        }

        return extraMinedInfo;
    }

    private void retrieveRepoLabels(GitRepo repo) throws IOException,InterruptedException {
        List<GitRepoLabel> repo_labels = new ArrayList<>();
        Response response = gitHubApiService.searchRepoLabels(repo.getName(),currentToken);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonArray results = JsonParser.parseString(responseBody.string()).getAsJsonArray();
            logger.info("\tAdding: "+results.size()+" labels.");
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
            logger.info("\tAdding: "+keySet.size()+" languages.");
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
