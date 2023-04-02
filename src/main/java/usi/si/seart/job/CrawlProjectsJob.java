package usi.si.seart.job;

import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.github.GitCommit;
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

import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

@Slf4j
@Service
@DependsOn("SupportedLanguageInitializationBean")
@ConditionalOnExpression(value = "${app.crawl.enabled:false} and not '${app.crawl.languages}'.isBlank()")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    private static final BinaryOperator<Date> DATE_MEDIAN = (a, b) -> new Date((a.getTime() + b.getTime())/2);

    Deque<Range<Date>> requestQueue = new ArrayDeque<>();

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
        List<String> languages = supportedLanguageService.getQueue().stream()
                .map(SupportedLanguage::getName)
                .collect(Collectors.toList());
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

        requestQueue.push(dateRange);
        do {
            long maxSize = 5;
            String nextIntervals = requestQueue.stream()
                    .limit(maxSize)
                    .map(range -> Ranges.toString(range, utcTimestampFormat))
                    .collect(Collectors.joining(", "));
            if (requestQueue.size() > maxSize) nextIntervals += ", ...";
            log.info("Next Crawl Intervals: [{}]", nextIntervals);

            Range<Date> first = requestQueue.pop();
            retrieveRepos(first, language, crawlUpdatedRepos);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(Range<Date> dateRange, String language, Boolean crawlUpdatedRepos) {
        int page = 1;
        try {
            JsonObject json = gitHubApiService.searchRepositories(language, dateRange, page, crawlUpdatedRepos);
            int totalResults = json.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults / 100.0);
            log.info("Retrieved results: " + totalResults);
            if (totalResults <= 1000) {
                JsonArray results = json.get("items").getAsJsonArray();
                saveRetrievedRepos(results, language, 1, totalResults);
                retrieveRemainingRepos(dateRange, language, crawlUpdatedRepos, totalPages);
                crawlJobService.updateCrawlDateForLanguage(language, dateRange.upperEndpoint());
            } else {
                List<Range<Date>> newIntervals = Ranges.split(dateRange, DATE_MEDIAN);
                if (newIntervals.size() > 1) {
                    requestQueue.push(newIntervals.get(1));
                    requestQueue.push(newIntervals.get(0));
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve repositories", e);
        }
    }

    private void retrieveRemainingRepos(
            Range<Date> dateRange, String language, Boolean crawlUpdatedRepos, int pages
    ) {
        if (pages > 1) {
            int page = 2;
            while (page <= pages) {
                try {
                    JsonObject json = gitHubApiService.searchRepositories(language, dateRange, page, crawlUpdatedRepos);
                    int totalResults = json.get("total_count").getAsInt();
                    JsonArray results = json.get("items").getAsJsonArray();
                    saveRetrievedRepos(results, language, (page - 1) * 100 + 1, totalResults);
                    page++;
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
        int repoNumEnd = repoNumStart + results.size() - 1;
        log.info(
                "Adding {} repositories ({} - {} | total: {})",
                results.size(),
                repoNumStart,
                repoNumEnd,
                repoNumTotal
        );
        for (JsonElement element : results) {
            JsonObject repoJson = element.getAsJsonObject();

            String repoFullName = repoJson.get("full_name").getAsString().toLowerCase();
            Optional<GitRepo> opt = gitRepoService.getByName(repoFullName);

            log.info(
                    "{} repository: {} [{}/{}]",
                    (opt.isEmpty()) ? "  Saving" : "Updating",
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
                JsonObject json = gitHubApiService.fetchRepoInfo(repoFullName);
                JsonElement jsonLanguage = json.get("language");
                if (jsonLanguage.isJsonNull()) {
                    // This can happen (e.g. https://api.github.com/repos/aquynh/iVM).
                    json.addProperty("language", language);
                }  else if (!jsonLanguage.getAsString().equals(language)) {
                    /*
                     * This can happen (e.g. https://api.github.com/search/repositories?q=baranowski/habit-vim).
                     * If you go to repository `homepage`, or the `language_url`
                     * (endpoint that shows language distribution),
                     * you will see that `main_language` is only wrong in the above link.
                    */
                    String format =
                            "Search language mismatch, while searching for {} repositories, " +
                            "the crawler encountered a repository erroneously reported as written in {}. " +
                            "Overwriting...";
                    log.warn(format, language, jsonLanguage.getAsString());
                    json.addProperty("language", language);
                }

                GitRepo repo = createRepoFromResponse(json);
                repo = gitRepoService.createOrUpdateRepo(repo);
                log.debug("\tAdding: Basic repository information.");
                retrieveRepoLabels(repo);
                retrieveRepoLanguages(repo);
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
    private GitRepo createRepoFromResponse(JsonObject json) {
        GitRepo gitRepo = conversionService.convert(json, GitRepo.class);

        String name = json.get("full_name").getAsString();
        boolean hasIssues = json.get("has_issues").getAsBoolean();

        Long commits = gitHubApiService.fetchNumberOfCommits(name);
        Long branches = gitHubApiService.fetchNumberOfBranches(name);
        Long releases = gitHubApiService.fetchNumberOfReleases(name);
        Long contributors = gitHubApiService.fetchNumberOfContributors(name);
        Long totalPullRequests = gitHubApiService.fetchNumberOfAllPulls(name);
        Long openPullRequests = gitHubApiService.fetchNumberOfOpenPulls(name);
        Long totalIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfAllIssuesAndPulls(name) - totalPullRequests;
        Long openIssues = (!hasIssues) ? 0L : gitHubApiService.fetchNumberOfOpenIssuesAndPulls(name) - openPullRequests;
        GitCommit gitCommit = gitHubApiService.fetchLastCommitInfo(name);
        Date lastCommit = gitCommit.getDate();
        String lastCommitSHA = gitCommit.getSha();

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
        List<GitRepoLabel> labels = new ArrayList<>();
        boolean store = false;
        try {
            Long count = gitHubApiService.fetchNumberOfLabels(repo.getName());
            int pages = (int) Math.ceil(count / 100.0);
            for (int page = 1; page <= pages; page++) {
                JsonArray objects = gitHubApiService.fetchRepoLabels(repo.getName(), page);
                log.debug("\tAdding: {} labels.", objects.size());

                for (JsonElement element : objects) {
                    JsonObject object = element.getAsJsonObject();
                    String label = object.get("name").getAsString();
                    label = label.trim();
                    label = label.substring(0, Math.min(label.length(), 60));  // 60: due to db column limit
                    labels.add(GitRepoLabel.builder().repo(repo).label(label).build());
                }

                store = true;
            }
            if (store) {
                gitRepoService.createUpdateLabels(repo, labels);
            }
        } catch (Exception e) {
            log.error("Failed to add repository labels", e);
        }
    }

    private void retrieveRepoLanguages(GitRepo repo) {
        List<GitRepoLanguage> languages = new ArrayList<>();
        boolean store = false;
        try {
            Long count = gitHubApiService.fetchNumberOfLanguages(repo.getName());
            int pages = (int) Math.ceil(count / 100.0);
            for (int page = 1; page <= pages; page++) {
                JsonObject result = gitHubApiService.fetchRepoLanguages(repo.getName(), page);
                Set<Map.Entry<String, JsonElement>> entries = result.entrySet();
                log.debug("\tAdding: {} languages.", entries.size());

                languages = entries.stream()
                        .map(entry -> GitRepoLanguage.builder()
                                .repo(repo)
                                .language(entry.getKey())
                                .sizeOfCode(entry.getValue().getAsLong())
                                .build())
                        .collect(Collectors.toList());

                store = true;
            }
            if (store) {
                gitRepoService.createUpdateLanguages(repo, languages);
            }
        } catch (Exception e) {
            log.error("Failed to add repository languages", e);
        }
    }
}
