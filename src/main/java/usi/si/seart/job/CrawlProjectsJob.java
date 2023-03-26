package usi.si.seart.job;

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
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.github.GitHubApiService;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoLabel;
import usi.si.seart.model.GitRepoLanguage;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.service.CrawlJobService;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.SupportedLanguageService;
import usi.si.seart.util.Dates;
import usi.si.seart.util.Ranges;

import java.io.IOException;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    private static final BinaryOperator<Date> dateMedian = (a, b) -> new Date((a.getTime() + b.getTime())/2);

    GitRepoService gitRepoService;
    CrawlJobService crawlJobService;
    SupportedLanguageService supportedLanguageService;

    ConversionService conversionService;

    GitHubApiService gitHubApiService;

    @NonFinal
    @Value(value = "${app.crawl.scheduling}")
    Long schedulingRate;

    @NonFinal
    @Value("#{new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\").parse(\"${app.crawl.startdate}\")}")
    Date defaultStartDate;

    DateFormat utcTimestampFormat;

    @Scheduled(fixedDelayString = "${app.crawl.scheduling}")
    public void run() {
        log.info("Initializing language queue...");
        languages.clear();
        supportedLanguageService.getQueue().stream()
                .map(SupportedLanguage::getName)
                .forEach(languages::add);
        log.info("Language crawling order: " + languages);
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
                    crawlJobService.updateCrawlDateForLanguage(language, dateRange.upperEndpoint());
                } else {
                    List<Range<Date>> newIntervals = Ranges.split(dateRange, dateMedian);
                    if (newIntervals.size() > 1) {
                        requestQueue.add(0, newIntervals.get(1));
                        requestQueue.add(0, newIntervals.get(0));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve repositories", e);
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
                    log.error("Failed to retrieve the remaining repositories", e);
                }
            }
        }
    }

    /**
     * Given JSON info of 100 repos, store them in DB
     */
    private void saveRetrievedRepos(JsonArray results, String language, int repoNumStart, int repoNumTotal) {
        log.info("Adding: " + results.size() + " repositories (" + repoNumStart + "-" + (repoNumStart + results.size() - 1) + " | total: " + repoNumTotal + ")");
        for (JsonElement element : results) {
            JsonObject repoJson = element.getAsJsonObject();

            String repoFullName = repoJson.get("full_name").getAsString().toLowerCase();
            Optional<GitRepo> opt = gitRepoService.getByName(repoFullName);

            log.info(
                    "{} repository: {} [{}/{}]",
                    (opt.isEmpty()) ? "Saving" : "Updating",
                    repoFullName,
                    repoNumStart,
                    repoNumTotal
            );

            repoNumStart++;

            // Optimization thing
            if (opt.isPresent()) {
                GitRepo existing = opt.get();
                if (hasNotBeenUpdated(existing, repoJson)) {
                    Date updatedAt = existing.getUpdatedAt();
                    Date pushedAt = existing.getPushedAt();
                    log.debug("\tSKIPPED: We already have the latest info!");
                    log.trace("\t\tUpdated: {}", updatedAt);
                    log.trace("\t\tPushed:  {}", pushedAt);
                    continue;
                }
            }

            try {
                String responseStr = gitHubApiService.fetchRepoInfo(repoFullName);
                if (responseStr != null) {
                    JsonObject result = JsonParser.parseString(responseStr).getAsJsonObject();

                    if (result.get("language").isJsonNull())
                        result.addProperty("language", language); // This can happen. Example Repo: "aquynh/iVM"
                    else if (!result.get("language").getAsString().equals(language)) {
                        // This can happen. Example Repo: https://api.github.com/search/repositories?q=baranowski/habit-vim
                        // And if you go to repo homepage or repo "language_url" (api that shows language distribution),
                        // you will see that main_language is only wrong in the above link.
                        log.warn("**** Mismatch language: searched-for: " + language + " | repo: " + repoJson.get("language").getAsString());
                        result.addProperty("language", language);
                    }

                    GitRepo repo = createRepoFromResponse(result);
                    repo = gitRepoService.createOrUpdateRepo(repo);
                    log.debug("\tBasic information saved (repo Table).");
                    retrieveRepoLabels(repo);
                    retrieveRepoLanguages(repo);
                } else {
                    log.error("SKIPPING due to null response from server");
                }
            } catch (Exception e) {
                log.error("Failed to save retrieved repositories", e);
            }
        }
    }

    private boolean hasNotBeenUpdated(GitRepo repo, JsonObject response) {
        Date dbUpdated = repo.getUpdatedAt();
        Date dbPushed = repo.getPushedAt();
        Date apiUpdated = Dates.fromGitDateString(response.get("updated_at").getAsString());
        Date apiPushed = Dates.fromGitDateString(response.get("pushed_at").getAsString());
        return dbUpdated.compareTo(apiUpdated) == 0 && dbPushed.compareTo(apiPushed) == 0;
    }

    @SuppressWarnings("ConstantConditions")
    private GitRepo createRepoFromResponse(JsonObject repoJson) throws IOException, InterruptedException {
        GitRepo gitRepo = conversionService.convert(repoJson, GitRepo.class);

        String repoName = repoJson.get("full_name").getAsString();
        boolean hasIssues = repoJson.get("has_issues").getAsBoolean();

        Long commits = gitHubApiService.fetchNumberOfCommits(repoName);
        Long branches = gitHubApiService.fetchNumberOfBranches(repoName);
        Long releases = gitHubApiService.fetchNumberOfReleases(repoName);
        Long contributors = gitHubApiService.fetchNumberOfContributors(repoName);
        Long totalPullRequests = gitHubApiService.fetchNumberOfAllPulls(repoName);
        Long openPullRequests = gitHubApiService.fetchNumberOfOpenPulls(repoName);
        Long totalIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfAllIssuesAndPulls(repoName) - totalPullRequests;
        Long openIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfOpenIssuesAndPulls(repoName) - openPullRequests;
        Pair<String, Date> lastCommitInfo = gitHubApiService.fetchLastCommitInfo(repoName);
        Date lastCommit = lastCommitInfo.getRight();
        String lastCommitSHA = lastCommitInfo.getLeft();

        gitRepo.setCommits(commits);
        gitRepo.setBranches(branches);
        gitRepo.setReleases(releases);
        gitRepo.setContributors(contributors);
        gitRepo.setTotalIssues(totalIssues);
        gitRepo.setOpenIssues(openIssues);
        gitRepo.setTotalPullRequests(totalPullRequests);
        gitRepo.setOpenPullRequests(openPullRequests);
        gitRepo.setLastCommit(lastCommit);
        gitRepo.setLastCommitSHA(lastCommitSHA);

        return gitRepo;
    }

    private void retrieveRepoLabels(GitRepo repo) {
        List<GitRepoLabel> repoLabels = new ArrayList<>();
        boolean newResults = false;
        try {
            Long totalLabels = gitHubApiService.fetchNumberOfLabels(repo.getName());
            int totalPages = (int) Math.ceil(totalLabels / 100.0);
            for (int page = 1; page <= totalPages; page++) {
                String responseStr = gitHubApiService.fetchRepoLabels(repo.getName(), page);
                if (responseStr != null) {
                    JsonArray result = JsonParser.parseString(responseStr).getAsJsonArray();
                    log.debug("\tAdding: " + result.size() + " labels.");

                    for (JsonElement item : result) {
                        String label = item.getAsJsonObject().get("name").getAsString();
                        label = label.trim();
                        label = label.substring(0, Math.min(label.length(), 60));  // 60: due to db column limit
                        repoLabels.add(GitRepoLabel.builder().repo(repo).label(label).build());
                    }
                    newResults = true;
                }
            }
            if (newResults) {
                gitRepoService.createUpdateLabels(repo, repoLabels);
            }
        } catch (Exception e) {
            log.error("Failed to add repository labels", e);
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
                    log.debug("\tAdding: " + keySet.size() + " languages.");

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
            log.error("Failed to add repository languages", e);
        }

    }
}
