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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.exception.UnsplittableRangeException;
import usi.si.seart.github.GitCommit;
import usi.si.seart.github.GitHubApiConnector;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoLabel;
import usi.si.seart.model.GitRepoLanguage;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.GitRepoTopicKey;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.model.Topic;
import usi.si.seart.service.CrawlJobService;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.GitRepoTopicsService;
import usi.si.seart.service.SupportedLanguageService;
import usi.si.seart.util.Dates;
import usi.si.seart.util.Optionals;
import usi.si.seart.util.Ranges;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@DependsOn("SupportedLanguageInitializationBean")
@ConditionalOnExpression(value = "${app.crawl.enabled:false} and not '${app.crawl.languages}'.isBlank()")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    Deque<Range<Date>> requestQueue = new ArrayDeque<>();

    GitRepoService gitRepoService;
    GitRepoTopicsService gitRepoTopicsService;
    CrawlJobService crawlJobService;
    SupportedLanguageService supportedLanguageService;

    ConversionService conversionService;

    GitHubApiConnector gitHubApiConnector;

    @NonFinal
    @Value(value = "${app.crawl.scheduling}")
    Long schedulingRate;

    @NonFinal
    @Value(value = "${app.crawl.startdate}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    Date defaultStartDate;

    Function<Date, String> dateStringMapper;

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
                    log.info(
                            "No previous crawling found for {}. We start from scratch: {}",
                            language, defaultStartDate
                    );
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

    private void crawlUpdatedRepos(Range<Date> range, String language) {
        log.info("Starting crawling {} repositories updated through: {}", language, range);
        crawlRepos(range, language);
        log.info("Finished crawling {} repositories updated through: {}", language, range);
    }

    private void crawlRepos(Range<Date> range, String language) {
        if (range.lowerEndpoint().compareTo(range.upperEndpoint()) >= 0) {
            log.warn("Invalid interval Skipped: " + range);
            return;
        }

        requestQueue.push(range);
        do {
            int limit = 5;
            int size = requestQueue.size();
            log.info("Next crawl intervals:");
            requestQueue.stream()
                    .limit(limit)
                    .map(item -> Ranges.toString(item, dateStringMapper))
                    .forEach(string -> log.info("\t[{}]", string));
            if (size > limit)
                log.info("\t{} omitted ...", size - limit);
            Range<Date> first = requestQueue.pop();
            retrieveRepos(first, language);
        } while (!requestQueue.isEmpty());
    }

    private void retrieveRepos(Range<Date> range, String language) {
        int page = 1;
        try {
            JsonObject json = gitHubApiConnector.searchRepositories(language, range, page);
            int totalResults = json.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults / 100.0);
            if (totalResults == 0) return;
            log.info("Retrieved results: " + totalResults);
            if (totalResults > 1000) {
                try {
                    Pair<Range<Date>, Range<Date>> ranges = Ranges.split(range, Dates::median);
                    requestQueue.push(ranges.getRight());
                    requestQueue.push(ranges.getLeft());
                    return;
                } catch (UnsplittableRangeException ure) {
                    log.warn(
                            "Encountered range that could not be further split [{}]!",
                            Ranges.toString(range, dateStringMapper)
                    );
                    log.info("Proceeding with mining anyway to mitigate data loss...");
                }
            }
            JsonArray results = json.get("items").getAsJsonArray();
            saveRetrievedRepos(results, language, 1, totalResults);
            retrieveRemainingRepos(range, language, totalPages);
            crawlJobService.updateCrawlDateForLanguage(language, range.upperEndpoint());
        } catch (Exception e) {
            log.error("Failed to retrieve repositories", e);
        }
    }

    private void retrieveRemainingRepos(Range<Date> range, String language, int pages) {
        if (pages > 1) {
            int page = 2;
            while (page <= pages) {
                try {
                    JsonObject json = gitHubApiConnector.searchRepositories(language, range, page);
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
    private void saveRetrievedRepos(JsonArray results, String language, int lowerIndex, int total) {
        int upperIndex = lowerIndex + results.size() - 1;
        log.info(
                "Crawling {} {} repositories ({} - {} | total: {})",
                results.size(), language, lowerIndex, upperIndex, total
        );

        for (JsonElement element : results) {
            JsonObject result = element.getAsJsonObject();
            String name = result.get("full_name").getAsString();
            Optional<GitRepo> optional = Optionals.ofThrowable(() -> gitRepoService.getByName(name));
            String action = optional.map(ignored -> "Updating").orElse("  Saving");
            log.info("{} repository: {} [{}/{}]", action, name, lowerIndex, total);

            lowerIndex++;

            // Optimization thing
            if (optional.isPresent()) {
                GitRepo existing = optional.get();
                if (hasNotBeenUpdated(existing, result)) {
                    Date updatedAt = existing.getUpdatedAt();
                    Date pushedAt = existing.getPushedAt();
                    log.debug("\tSKIPPED: We already have the latest info!");
                    log.trace("\t\tUpdated: {}", updatedAt);
                    log.trace("\t\tPushed:  {}", pushedAt);
                    continue;
                }
            }

            try {
                JsonObject json = gitHubApiConnector.fetchRepoInfo(name);
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
                retrieveRepoTopics(repo);
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

        Long commits = gitHubApiConnector.fetchNumberOfCommits(name);
        Long branches = gitHubApiConnector.fetchNumberOfBranches(name);
        Long releases = gitHubApiConnector.fetchNumberOfReleases(name);
        Long contributors = gitHubApiConnector.fetchNumberOfContributors(name);
        Long totalPullRequests = gitHubApiConnector.fetchNumberOfAllPulls(name);
        Long openPullRequests = gitHubApiConnector.fetchNumberOfOpenPulls(name);
        Long totalIssues = (hasIssues)
                ? gitHubApiConnector.fetchNumberOfAllIssuesAndPulls(name) - totalPullRequests
                : 0L;
        Long openIssues = (hasIssues)
                ? gitHubApiConnector.fetchNumberOfOpenIssuesAndPulls(name) - openPullRequests
                : 0L;
        GitCommit gitCommit = gitHubApiConnector.fetchLastCommitInfo(name);
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

        gitRepo.setCrawled();

        return gitRepo;
    }

    private void retrieveRepoLabels(GitRepo repo) {
        List<GitRepoLabel> labels = new ArrayList<>();
        boolean store = false;
        try {
            Long count = gitHubApiConnector.fetchNumberOfLabels(repo.getName());
            int pages = (int) Math.ceil(count / 100.0);
            for (int page = 1; page <= pages; page++) {
                JsonArray objects = gitHubApiConnector.fetchRepoLabels(repo.getName(), page);
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
            Long count = gitHubApiConnector.fetchNumberOfLanguages(repo.getName());
            int pages = (int) Math.ceil(count / 100.0);
            for (int page = 1; page <= pages; page++) {
                JsonObject result = gitHubApiConnector.fetchRepoLanguages(repo.getName(), page);
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

    private void retrieveRepoTopics(GitRepo repo) {
        List<GitRepoTopic> topics = new ArrayList<>();
        boolean store = false;
        try {
            Long count = gitHubApiConnector.fetchNumberOfTopics(repo.getName());
            int pages = (int) Math.ceil(count / 100.0);
            for (int page = 1; page <= pages; page++) {
                JsonObject result = gitHubApiConnector.fetchRepoTopics(repo.getName(), page);
                JsonArray names = result.getAsJsonArray("names");
                log.debug("\tAdding: {} topics.", names.size());

                topics = StreamSupport.stream(names.spliterator(), true)
                        .map(entry -> {
                            Topic topic = gitRepoTopicsService.getOrCreateTopic(entry.getAsString());
                            return GitRepoTopic.builder()
                                    .id(new GitRepoTopicKey(repo.getId(), topic.getId()))
                                    .repo(repo)
                                    .topic(topic)
                                    .build();
                        })
                        .collect(Collectors.toList());

                store = true;
            }
            if (store) {
                gitRepoTopicsService.createOrUpdateGitRepoTopics(repo, topics);
            }
        } catch (Exception e) {
            log.error("Failed to add repository topics", e);
        }
    }
}
