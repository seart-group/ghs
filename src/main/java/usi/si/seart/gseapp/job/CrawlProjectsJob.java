package usi.si.seart.gseapp.job;

import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.db_access_service.CrawlJobService;
import usi.si.seart.gseapp.db_access_service.GitRepoService;
import usi.si.seart.gseapp.db_access_service.SupportedLanguageService;
import usi.si.seart.gseapp.github_service.GitHubApiService;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;
import usi.si.seart.gseapp.model.SupportedLanguage;
import usi.si.seart.gseapp.util.Dates;
import usi.si.seart.gseapp.util.Ranges;

import java.io.IOException;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    List<Range<Date>> requestQueue = new ArrayList<>();

    List<String> languages = new ArrayList<>();

    static BinaryOperator<Date> dateMedian = (a, b) -> new Date((a.getTime() + b.getTime())/2);

    GitRepoService gitRepoService;
    CrawlJobService crawlJobService;
    SupportedLanguageService supportedLanguageService;

    GitHubApiService gitHubApiService;

    @NonFinal
    @Value(value = "${app.crawl.scheduling}")
    Long schedulingRate;

    @NonFinal
    @Value("#{new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\").parse(\"${app.crawl.startdate}\")}")
    Date defaultStartDate;

    DateFormat utcTimestampFormat;

    @Scheduled(fixedDelayString = "${app.crawl.scheduling}")
    public void run() throws IOException, InterruptedException {
        List<SupportedLanguage> supportedLanguages = supportedLanguageService.getAll();
        supportedLanguages.sort(Comparator.comparing(SupportedLanguage::getAdded).reversed());
        languages.clear();
        languages.addAll(
                supportedLanguages.stream()
                        .map(SupportedLanguage::getName)
                        .collect(Collectors.toList())
        );

        log.info("New Crawling for all languages: " + languages);
        Date endDate = Date.from(Instant.now().minus(Duration.ofHours(2)));

        for (String language : languages) {
            this.requestQueue.clear();
            Date startDate = crawlJobService.getCrawlDateByLanguage(language);
            Range<Date> dateRange;

            try {
                if (startDate != null) {
                    assert startDate.before(endDate);
                    dateRange = Ranges.build(startDate, endDate);
                } else {
                    log.info("No previous crawling found for {}. We start from scratch: {}", language, defaultStartDate);
                    dateRange = Ranges.build(defaultStartDate, endDate);
                }
            } catch (IllegalArgumentException ex) {
                // Handler for cases where Start > End
                log.warn("Language {} has bad range: {}", language, ex.getMessage());
                continue;
            }

            crawlUpdatedRepos(dateRange, language);
        }
        log.info("Next crawl scheduled for: {}", Date.from(Instant.now().plusMillis(schedulingRate)));
    }

    private void crawlCreatedRepos(Range<Date> interval, String language) {
        log.info("Starting crawling {} repositories created through: {}", language, interval);
        crawlRepos(interval, language, false);
        log.info("Finished crawling {} repositories created through: {}", language, interval);
    }

    private void crawlUpdatedRepos(Range<Date> dateRange, String language) {
        log.info("Starting crawling {} repositories updated through: {}", language, dateRange);
        crawlRepos(dateRange, language, true);
        log.info("Finished crawling {} repositories updated through: {}", language, dateRange);
    }

    private void crawlRepos(Range<Date> dateRange, String language, Boolean crawlUpdatedRepos) {
        if (dateRange.lowerEndpoint().compareTo(dateRange.upperEndpoint()) >= 0) {
            log.warn("Invalid interval Skipped: " + dateRange);
            return;
        }

        requestQueue.add(dateRange);
        do {
            long maxSize = 5;
            String nextIntervals = requestQueue.stream()
                    .limit(maxSize)
                    .map(range -> Ranges.toString(range, utcTimestampFormat))
                    .collect(Collectors.joining(", "));
            if (requestQueue.size() > maxSize) nextIntervals += ", ...";
            log.info("Next Crawl Intervals: [{}]", nextIntervals);

            Range<Date> first = requestQueue.remove(0);
            retrieveRepos(first, language, crawlUpdatedRepos);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(Range<Date> dateRange, String language, Boolean crawlUpdatedRepos) {
        int page = 1;
        try {
            String responseStr = gitHubApiService.searchRepositories(language, dateRange, page, crawlUpdatedRepos);
            if (responseStr != null) {
                JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                int totalResults = result.get("total_count").getAsInt();
                int totalPages = (int) Math.ceil(totalResults / 100.0);
                log.info("Retrieved results: " + totalResults);
                if (totalResults <= 1000) {
                    JsonArray results = result.get("items").getAsJsonArray();
                    saveRetrievedRepos(results, language, 1, totalResults);
                    retrieveRemainingRepos(dateRange, language, crawlUpdatedRepos, totalPages);
                    crawlJobService.updateCrawlDateForLanguage(language, dateRange.lowerEndpoint());
                } else {
                    List<Range<Date>> newIntervals = Ranges.split(dateRange, dateMedian);
                    if (newIntervals.size() > 1) {
                        requestQueue.add(0, newIntervals.get(1));
                        requestQueue.add(0, newIntervals.get(0));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse GitHubAPI response {} (retrieveRepos)", e.getMessage());
        }
    }

    private void retrieveRemainingRepos(Range<Date> dateRange, String language, Boolean crawlUpdatedRepos, int totalPages){
        if (totalPages > 1) {
            int page = 2;
            while (page <= totalPages) {
                try {
                    String responseStr = gitHubApiService.searchRepositories(language, dateRange, page, crawlUpdatedRepos);
                    if (responseStr != null) {
                        JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                        int totalResults = result.get("total_count").getAsInt();
                        JsonArray results = result.get("items").getAsJsonArray();
                        saveRetrievedRepos(results, language, (page - 1) * 100 + 1, totalResults);
                        page++;
                    }
                } catch (Exception e) {
                    log.error("Failed to parse GitHubAPI response {} (retrieveRemainingRepos)", e.getMessage());
                }
            }
        }
    }

    /**
     * Given JSON info of 100 repos, store them in DB
     */
    private void saveRetrievedRepos(JsonArray results, String language, int repo_num_start, int repo_num_total) {
        log.info("Adding: " + results.size() + " repositories (" + repo_num_start + "-" + (repo_num_start + results.size() - 1) + " | total: " + repo_num_total + ")");
        for (JsonElement element : results) {
            JsonObject repoJson = element.getAsJsonObject();

            String repoFullName = repoJson.get("full_name").getAsString().toLowerCase();
            Optional<GitRepo> opt = gitRepoService.getByName(repoFullName);

            if (opt.isEmpty()) {
                log.info(repo_num_start + "/" + repo_num_total + " saving new repo: " + repoFullName);
            } else {
                log.info(repo_num_start + "/" + repo_num_total + " updating repo: " + repoFullName);
            }

            repo_num_start++;

            // Optimization thing
            if (opt.isPresent()) {
                GitRepo existingRepInfo = opt.get();
                Date existing_updatedAt = existingRepInfo.getUpdatedAt();
                Date existing_pushedAt = existingRepInfo.getPushedAt();

                Date repo_updatedAt = Dates.fromGitDateString(repoJson.get("updated_at").getAsString());
                Date repo_pushedAt = Dates.fromGitDateString(repoJson.get("pushed_at").getAsString());

                // boolean incompleteMinedInfo = existingRepInfo.getContributors() == null;

                if (existing_updatedAt.compareTo(repo_updatedAt) == 0 && existing_pushedAt.compareTo(repo_pushedAt) == 0) {
                    log.info("\tSKIPPED. We already have the latest info up to " + existing_updatedAt + "(updated)  " + existing_pushedAt + "(pushed)");
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
                        log.warn("**** Mismatch language: searched-for: " + language + " | repo: " + repoJson.get("language").getAsString());
                        result.addProperty("language", language);
                    }

                    GitRepo repo = createGitRepoRowObjectFromGitHubAPIResultJson(result);
                    repo = gitRepoService.createOrUpdateRepo(repo);
                    if (repo != null) {
                        log.info("\tBasic information saved (repo Table).");
                        retrieveRepoLabels(repo);
                        retrieveRepoLanguages(repo);
                    }
                } else {
                    log.error("SKIPPING due to null response from server");
                }
            } catch (Exception e) {
                log.error("Failed to fetch repo info from GitHubAPI: {} (saveRetrievedRepos)", e.getMessage());
            }
        }
    }

    // TODO: 03.02.22 Migrate this into a converter! We can use something like JsonObjectToGitRepoConverter...
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
                .replace("\"", ""));
        gitRepoBuilder.stargazers(repoJson.get("stargazers_count").getAsLong());
        gitRepoBuilder.forks(repoJson.get("forks_count").getAsLong());
        gitRepoBuilder.watchers(repoJson.get("subscribers_count").getAsLong());
        gitRepoBuilder.size(repoJson.get("size").getAsLong());
        gitRepoBuilder.createdAt(Dates.fromGitDateString(repoJson.get("created_at").getAsString()));
        gitRepoBuilder.pushedAt(Dates.fromGitDateString(repoJson.get("pushed_at").getAsString()));
        gitRepoBuilder.updatedAt(Dates.fromGitDateString(repoJson.get("updated_at").getAsString()));
        gitRepoBuilder.homepage(homepage.isJsonNull() ? null : homepage.getAsString());
        gitRepoBuilder.mainLanguage(repoJson.get("language").getAsString());
        gitRepoBuilder.hasWiki(repoJson.get("has_wiki").getAsBoolean());
        gitRepoBuilder.isArchived(repoJson.get("archived").getAsBoolean());
        boolean hasIssues = repoJson.get("has_issues").getAsBoolean();
        // // open_issues in the response refers to sum of "issues" and "pull requests"
        // gitRepoBuilder.openIssues(repoJson.get("open_issues").getAsLong());


        Long numberOfCommits = gitHubApiService.fetchNumberOfCommits(repoFullName);
        Long numberOfBranches = gitHubApiService.fetchNumberOfBranches(repoFullName);
        Long numberOfReleases = gitHubApiService.fetchNumberOfReleases(repoFullName);
        Long numberOfContributors = gitHubApiService.fetchNumberOfContributors(repoFullName);
        Long numberOfAllPulls = gitHubApiService.fetchNumberOfAllPulls(repoFullName);
        Long numberOfOpenPulls = gitHubApiService.fetchNumberOfOpenPulls(repoFullName);
        Long numberOfAllIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfAllIssuesAndPulls(repoFullName) - numberOfAllPulls;
        Long numberOfOpenIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfOpenIssuesAndPulls(repoFullName) - numberOfOpenPulls;
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
        boolean newResults = false;
        try {
            Long totalLabels = gitHubApiService.fetchNumberOfLabels(repo.getName());
            int totalPages = (int) Math.ceil(totalLabels / 100.0);
            for(int page=1; page<=totalPages; page++)
            {
                String responseStr = gitHubApiService.fetchRepoLabels(repo.getName(), page);
                if (responseStr != null) {
                    JsonArray result = JsonParser.parseString(responseStr).getAsJsonArray();
                    log.info("\tAdding: " + result.size() + " labels.");

                    for (JsonElement item : result) {
                        String label = item.getAsJsonObject().get("name").getAsString();
                        label = label.trim();
                        label = label.substring(0, Math.min(label.length(), 60));  // 60: due to db column limit
                        repo_labels.add(GitRepoLabel.builder().repo(repo).label(label).build());
                    }
                    newResults = true;
                }
            }
            if (newResults) {
                gitRepoService.createUpdateLabels(repo, repo_labels);
            }
        } catch (Exception e) {
            log.error("Failed to add labels: {}", e.getMessage());
        }
    }

    private void retrieveRepoLanguages(GitRepo repo) {
        List<GitRepoLanguage> repoLanguages = new ArrayList<>();
        boolean newResults = false;
        try {
            Long totalLanguages = gitHubApiService.fetchNumberOfLanguages(repo.getName());
            int totalPages = (int) Math.ceil(totalLanguages / 100.0);
            for (int page = 1; page <= totalPages; page++) {
                String responseStr = gitHubApiService.fetchRepoLanguages(repo.getName(), page);
                if (responseStr != null) {
                    JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();
                    Set<String> keySet = result.keySet();
                    log.info("\tAdding: " + keySet.size() + " languages.");

                    keySet.forEach(key -> repoLanguages.add(GitRepoLanguage.builder()
                            .repo(repo)
                            .language(key)
                            .sizeOfCode(result.get(key).getAsLong())
                            .build())
                    );
                    newResults = true;
                }
            }
            if (newResults) {
                gitRepoService.createUpdateLanguages(repo, repoLanguages);
            }
        } catch (Exception e) {
            log.error("Failed to add languages: {}", e.getMessage());
        }

    }
}
