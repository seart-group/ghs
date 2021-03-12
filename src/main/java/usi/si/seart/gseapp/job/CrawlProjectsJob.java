package usi.si.seart.gseapp.job;

import usi.si.seart.gseapp.github_service.GitHubApiService;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;
import usi.si.seart.gseapp.repository.GitRepoRepository;
import usi.si.seart.gseapp.repository.SupportedLanguageRepository;
import usi.si.seart.gseapp.db_access_service.ApplicationPropertyService;
import usi.si.seart.gseapp.db_access_service.CrawlJobService;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.util.DateUtils;
import usi.si.seart.gseapp.util.interval.DateInterval;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    static Logger logger = LoggerFactory.getLogger(CrawlProjectsJob.class);


    @NonFinal
    public boolean running = false;
    List<DateInterval> requestQueue = new ArrayList<>();

    List<String> languages = new ArrayList<>();

    @NonFinal
    // Temporary. Because I'm keep restarting server, but I don't care about
    // very new Java updates, but finishing all language at least once.
    static String startingLanguage = null;


    ;
    SupportedLanguageRepository supportedLanguageRepository;
    GitRepoRepository gitRepoRepository;

    GitHubApiService gitHubApiService;
    GitRepoService gitRepoService;
    CrawlJobService crawlJobService;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public CrawlProjectsJob(SupportedLanguageRepository supportedLanguageRepository,
                            GitHubApiService gitHubApiService,
                            GitRepoService gitRepoService,
                            CrawlJobService crawlJobService,
                            ApplicationPropertyService applicationPropertyService,
                            GitRepoRepository gitRepoRepository) {

        this.supportedLanguageRepository = supportedLanguageRepository;
        this.gitHubApiService = gitHubApiService;
        this.gitRepoService = gitRepoService;
        this.crawlJobService = crawlJobService;
        this.applicationPropertyService = applicationPropertyService;
        this.gitRepoRepository = gitRepoRepository;
    }

    public void run() throws IOException, InterruptedException {
        this.running = true;
        getLanguagesToMine();

        logger.info("New Crawling for all languages: " + languages);
        Date endDate = Date.from(Instant.now().minus(Duration.ofHours(2)));

        for (String language : languages) {

//            if(language.equals("JavaScript"))
//                continue; // Temporary

            if (language.equals(startingLanguage))
                startingLanguage = null;
            else if (startingLanguage != null && !language.equals(startingLanguage))
                continue;

            this.requestQueue.clear();
            Date startDate = crawlJobService.getCrawlDateByLanguage(language);
            DateInterval interval;

            if (startDate != null) {
                assert startDate.before(endDate);
                interval = DateInterval.builder().start(startDate).end(endDate).build();
            } else {
                Date veryStartDate = applicationPropertyService.getStartDate();
                logger.info("No previous crawling found for " + language + ". We start from scratch: " + veryStartDate);
                interval = DateInterval.builder().start(veryStartDate).end(endDate).build();
            }

            if (interval.getStart().after(interval.getStart())) {
                logger.warn("language " + language + " has bad interval range: Start > End | " + interval.getStart() + " > " + interval.getEnd());
                continue;
            }
            crawlUpdatedRepos(interval, language);
        }
        this.running = false;
    }

    private void crawlCreatedRepos(DateInterval interval, String language) throws IOException, InterruptedException {
        logger.info("Starting crawling " + language + " repositories created through: " + interval);
        crawlRepos(interval, language, false);
        logger.info("Finished crawling " + language + " repositories created through: " + interval);
    }

    private void crawlUpdatedRepos(DateInterval interval, String language) throws IOException, InterruptedException {
        logger.info("Starting crawling " + language + " repositories updated through: " + interval);
        crawlRepos(interval, language, true);
        logger.info("Finished crawling " + language + " repositories updated through: " + interval);
    }

    private void crawlRepos(DateInterval interval, String language, Boolean crawl_updated_repos)
            throws IOException, InterruptedException {
        if (interval.getStart().compareTo(interval.getEnd()) >= 0) {
            logger.warn("Invalid interval Skipped: " + interval);
            return;
        }

        requestQueue.add(interval);
        do {
            logger.info("Next Crawl Intervals: " + requestQueue.toString());

            DateInterval first = requestQueue.remove(0);
            retrieveRepos(first, language, crawl_updated_repos);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(DateInterval interval, String language, Boolean crawl_updated_repos) {
        int page = 1;
        try {
            String responseStr = gitHubApiService.searchRepositories(language, interval, page, crawl_updated_repos);
            if (responseStr != null) {
                JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                int totalResults = result.get("total_count").getAsInt();
                int totalPages = (int) Math.ceil(totalResults / 100.0);
                logger.info("Retrieved results: " + totalResults);
                if (totalResults <= 1000) {
                    JsonArray results = result.get("items").getAsJsonArray();
                    saveRetrievedRepos(results, language, 1, totalResults);
                    retrieveRemainingRepos(interval, language, crawl_updated_repos, totalPages);
                    crawlJobService.updateCrawlDateForLanguage(language, interval.getEnd());
                } else {
                    Pair<DateInterval, DateInterval> newIntervals = interval.splitInterval();
                    if (newIntervals != null) {
                        requestQueue.add(0, newIntervals.getRight());
                        requestQueue.add(0, newIntervals.getLeft());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse GitHubAPI response {} (retrieveRepos)", e.getMessage());
        }
    }

    private void retrieveRemainingRepos(DateInterval interval, String language, Boolean crawl_updated_repos, int totalPages){
        if (totalPages > 1) {
            int page = 2;
            while (page <= totalPages) {
                try {
                    String responseStr = gitHubApiService.searchRepositories(language, interval, page, crawl_updated_repos);
                    if (responseStr != null) {
                        JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                        int totalResults = result.get("total_count").getAsInt();
                        JsonArray results = result.get("items").getAsJsonArray();
                        saveRetrievedRepos(results, language, (page - 1) * 100 + 1, totalResults);
                        page++;
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse GitHubAPI response {} (retrieveRemainingRepos)", e.getMessage());
                }
            }
        }
    }

    /**
     * Given JSON info of 100 repos, store them in DB
     */
    private void saveRetrievedRepos(JsonArray results, String language, int repo_num_start, int repo_num_total) {
        logger.info("Adding: " + results.size() + " repositories (" + repo_num_start + "-" + (repo_num_start + results.size() - 1) + " | total: " + repo_num_total + ")");
        for (JsonElement element : results) {
            JsonObject repoJson = element.getAsJsonObject();

            String repoFullName = repoJson.get("full_name").getAsString().toLowerCase();
            Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repoFullName);

            if (!opt.isPresent())
                logger.info(repo_num_start + "/" + repo_num_total + " saving new repo: " + repoFullName);
            else
                logger.info(repo_num_start + "/" + repo_num_total + " updating repo: " + repoFullName);

            repo_num_start++;

            // Optimization thing
            if (opt.isPresent()) {
                GitRepo existingRepInfo = opt.get();
                Date existing_updatedAt = existingRepInfo.getUpdatedAt();
                Date existing_pushedAt = existingRepInfo.getPushedAt();

                Date repo_updatedAt = DateUtils.fromGitDateString(repoJson.get("updated_at").getAsString());
                Date repo_pushedAt = DateUtils.fromGitDateString(repoJson.get("pushed_at").getAsString());

//                boolean incompleteMinedInfo = existingRepInfo.getContributors() == null;

                if (existing_updatedAt.compareTo(repo_updatedAt) == 0 && existing_pushedAt.compareTo(repo_pushedAt) == 0) {
                    logger.info("\tSKIPPED. We already have the latest info up to " + existing_updatedAt + "(updated)  " + existing_pushedAt + "(pushed)");
                    continue; // we already have the latest info for this repo
                }
            }

            try {
                String responseStr = gitHubApiService.fetchRepoInfo(repoFullName);
                if (responseStr != null) {
                    JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();


                    if (result.get("language").isJsonNull())
                        result.addProperty("language", language); // This can happen. Example Repo: "aquynh/iVM"
                    else if (false == result.get("language").getAsString().equals(language)) {
                        // This can happen. Example Repo: https://api.github.com/search/repositories?q=baranowski/habit-vim
                        // And if you go to repo homepage or repo "language_url" (api that shows language distribution),
                        // you will see that main_language is only wrong in the above link.
                        logger.warn("**** Mismatch language: searched-for: " + language + " | repo: " + repoJson.get("language").getAsString());
                        result.addProperty("language", language);
                    }

                    GitRepo repo = createGitRepoRowObjectFromGitHubAPIResultJson(result);
                    repo = gitRepoService.createOrUpdateRepo(repo);
                    if (repo != null) {
                        logger.info("\tBasic information saved (repo Table).");
                        retrieveRepoLabels(repo);
                        retrieveRepoLanguages(repo);
                    }
                } else {
                    logger.error("SKIPPING due to null response from server");
                }
            } catch (Exception e) {
                logger.error("Failed to fetch repo info from GitHubAPI: {} (saveRetrievedRepos)", e.getMessage());
            }
        }
    }

    private GitRepo createGitRepoRowObjectFromGitHubAPIResultJson(JsonObject repoJson) throws IOException, InterruptedException {
        GitRepo.GitRepoBuilder gitRepoBuilder = GitRepo.builder();

        String repoFullName = repoJson.get("full_name").getAsString();

        JsonElement license = repoJson.get("license");
        JsonElement homepage = repoJson.get("homepage");

        gitRepoBuilder.name(repoFullName.toLowerCase());
        gitRepoBuilder.isFork(repoJson.get("fork").getAsBoolean());
        gitRepoBuilder.defaultBranch(repoJson.get("default_branch").getAsString());
        gitRepoBuilder.license((license.isJsonNull()) ? null : license.getAsJsonObject()
                .get("name")
                .getAsString()
                .replaceAll("\"", ""));
        gitRepoBuilder.stargazers(repoJson.get("stargazers_count").getAsLong());
        gitRepoBuilder.forks(repoJson.get("forks_count").getAsLong());
        gitRepoBuilder.watchers(repoJson.get("subscribers_count").getAsLong());
        gitRepoBuilder.size(repoJson.get("size").getAsLong());
        gitRepoBuilder.createdAt(DateUtils.fromGitDateString(repoJson.get("created_at").getAsString()));
        gitRepoBuilder.pushedAt(DateUtils.fromGitDateString(repoJson.get("pushed_at").getAsString()));
        gitRepoBuilder.updatedAt(DateUtils.fromGitDateString(repoJson.get("updated_at").getAsString()));
        gitRepoBuilder.homepage(homepage.isJsonNull() ? null : homepage.getAsString());
        gitRepoBuilder.mainLanguage(repoJson.get("language").getAsString());
        gitRepoBuilder.hasWiki(repoJson.get("has_wiki").getAsBoolean());
        gitRepoBuilder.isArchived(repoJson.get("archived").getAsBoolean());
        boolean has_issues = repoJson.get("has_issues").getAsBoolean();
        //gitRepoBuilder.openIssues(repoJson.get("open_issues").getAsLong());  // open_issues in the response refers to sum of "issues" and "pull requests"


        Long numberOfCommits = gitHubApiService.fetchNumberOfCommits(repoFullName);
        Long numberOfBranches = gitHubApiService.fetchNumberOfBranches(repoFullName);
        Long numberOfReleases = gitHubApiService.fetchNumberOfReleases(repoFullName);
        Long numberOfContributors = gitHubApiService.fetchNumberOfContributors(repoFullName);
        Long numberOfAllPulls = gitHubApiService.fetchNumberOfAllPulls(repoFullName);
        Long numberOfOpenPulls = gitHubApiService.fetchNumberOfOpenPulls(repoFullName);
        Long numberOfAllIssues = (!has_issues) ? 0L : gitHubApiService.fetchNumberOfAllIssuesAndPulls(repoFullName) - numberOfAllPulls;
        Long numberOfOpenIssues = (!has_issues) ? 0L : gitHubApiService.fetchNumberOfOpenIssuesAndPulls(repoFullName) - numberOfOpenPulls;
        Pair<String, Date> lastCommitInfo = gitHubApiService.fetchLastCommitInfo(repoFullName);
        String lastCommitSHA = lastCommitInfo.getLeft();
        Date lastCommitDate = lastCommitInfo.getRight();

        gitRepoBuilder.commits(numberOfCommits);
        gitRepoBuilder.branches(numberOfBranches);
        gitRepoBuilder.releases(numberOfReleases);
        gitRepoBuilder.contributors(numberOfContributors);
        gitRepoBuilder.totalIssues(numberOfAllIssues);
        gitRepoBuilder.openIssues(numberOfOpenIssues);
        gitRepoBuilder.totalPullRequests(numberOfAllPulls);
        gitRepoBuilder.openPullRequests(numberOfOpenPulls);
        gitRepoBuilder.lastCommit(lastCommitDate);
        gitRepoBuilder.lastCommitSHA(lastCommitSHA);

        return gitRepoBuilder.build();
    }

    private void retrieveRepoLabels(GitRepo repo) {
        List<GitRepoLabel> repo_labels = new ArrayList<>();
        try {
            String responseStr = gitHubApiService.fetchRepoLabels(repo.getName());
            if (responseStr != null) {
                JsonArray result = JsonParser.parseString(responseStr).getAsJsonArray();
                logger.info("\tAdding: " + result.size() + " labels.");

                for (JsonElement item : result) {
                    String label = item.getAsJsonObject().get("name").getAsString();
                    label = label.trim();
                    label = label.substring(0, Math.min(label.length(), 60));  // 60: due to db column limit
                    repo_labels.add(GitRepoLabel.builder().repo(repo).label(label).build());
                }

                gitRepoService.createUpdateLabels(repo, repo_labels);
            }
        } catch (Exception e) {
            logger.error("Failed to add labels: {}", e.getMessage());
        }
    }

    private void retrieveRepoLanguages(GitRepo repo) {
        List<GitRepoLanguage> repo_languages = new ArrayList<>();

        try {
            String responseStr = gitHubApiService.fetchRepoLanguages(repo.getName());
            if (responseStr != null) {
                JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                Set<String> keySet = result.keySet();
                logger.info("\tAdding: " + keySet.size() + " languages.");

                keySet.forEach(key -> repo_languages.add(GitRepoLanguage.builder()
                        .repo(repo)
                        .language(key)
                        .sizeOfCode(result.get(key).getAsLong())
                        .build())
                );

                gitRepoService.createUpdateLanguages(repo, repo_languages);
            }
        } catch (Exception e) {
            logger.error("Failed to add languages: {}", e.getMessage());
        }

    }

    private void getLanguagesToMine() {
        languages.clear();
        supportedLanguageRepository.findAll().forEach(language -> languages.add(language.getName()));
    }
}
