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
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import usi.si.seart.exception.MetadataCrawlingException;
import usi.si.seart.exception.UnsplittableRangeException;
import usi.si.seart.github.GitCommit;
import usi.si.seart.github.GitHubAPIConnector;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.Label;
import usi.si.seart.model.Language;
import usi.si.seart.model.Topic;
import usi.si.seart.model.join.GitRepoLanguage;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.LabelService;
import usi.si.seart.service.LanguageService;
import usi.si.seart.service.TopicService;
import usi.si.seart.util.Dates;
import usi.si.seart.util.Optionals;
import usi.si.seart.util.Ranges;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@DependsOn("LanguageInitializationBean")
@ConditionalOnExpression(value = "${app.crawl.enabled:false} and not '${app.crawl.languages}'.isBlank()")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob {

    Deque<Range<Date>> requestQueue = new ArrayDeque<>();

    GitRepoService gitRepoService;
    TopicService topicService;
    LabelService labelService;
    LanguageService languageService;

    ConversionService conversionService;

    GitHubAPIConnector gitHubApiConnector;

    @NonFinal
    @Value(value = "${app.crawl.scheduling}")
    Duration schedulingRate;

    Function<Date, String> dateStringMapper;

    @Scheduled(fixedDelayString = "${app.crawl.scheduling}")
    public void run() {
        log.info("Initializing language queue...");
        Collection<Language> languages = languageService.getTargetedLanguages();
        log.info("Language crawling order: {}", languages.stream().map(Language::getName).collect(Collectors.toList()));
        for (Language language : languages) {
            this.requestQueue.clear();
            Language.Progress progress = languageService.getProgress(language);
            Date startDate = progress.getCheckpoint();
            Date endDate = Date.from(Instant.now().minus(Duration.ofHours(2)));
            Range<Date> dateRange = Ranges.build(startDate, endDate);
            crawlRepositories(dateRange, language);
        }
        log.info("Next crawl scheduled for: {}", Date.from(Instant.now().plus(schedulingRate)));
    }

    private void crawlRepositories(Range<Date> range, Language language) {
        log.info("Starting crawling {} repositories updated through: {}", language.getName(), range);
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
            retrieveRepositories(first, language);
        } while (!requestQueue.isEmpty());
        log.info("Finished crawling {} repositories updated through: {}", language.getName(), range);
    }

    private void retrieveRepositories(Range<Date> range, Language language) {
        String name = language.getName();
        int page = 1;
        try {
            JsonObject json = gitHubApiConnector.searchRepositories(name, range, page);
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
            saveRetrievedRepos(results, name, 1, totalResults);
            retrieveRemainingRepos(range, name, totalPages);
            Date checkpoint = range.upperEndpoint();
            log.info("{} repositories crawled up to: {}", name, checkpoint);
            languageService.updateProgress(language, checkpoint);
        } catch (NonTransientDataAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to retrieve repositories", ex);
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
                } catch (NonTransientDataAccessException ex) {
                    throw ex;
                } catch (Exception ex) {
                    log.error("Failed to retrieve the remaining repositories", ex);
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
            String action = optional.map(ignored -> "Updating:  ").orElse("Saving:    ");
            log.info("{}{} [{}/{}]", action, name, lowerIndex, total);

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
                } else if (!jsonLanguage.getAsString().equals(language)) {
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

                Set<Label> labels = retrieveRepoLabels(repo);
                if (!labels.isEmpty())
                    log.debug("\tAdding: {} labels.", labels.size());
                repo.setLabels(labels);

                Set<GitRepoLanguage> languages = retrieveRepoLanguages(repo);
                if (!languages.isEmpty())
                    log.debug("\tAdding: {} languages.", languages.size());
                repo.setLanguages(languages);

                Set<Topic> topics = retrieveTopics(repo);
                if (!topics.isEmpty())
                    log.debug("\tAdding: {} topics.", topics.size());
                repo.setTopics(topics);

                gitRepoService.updateRepo(repo);
            } catch (NonTransientDataAccessException ex) {
                throw ex;
            } catch (Exception ex) {
                log.error("Failed to save repository", ex);
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

        Language language = languageService.getOrCreate(json.get("language").getAsString());

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

        gitRepo.setMainLanguage(language);
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

    private Set<Label> retrieveRepoLabels(GitRepo repo) {
        try {
            JsonArray array = gitHubApiConnector.fetchRepoLabels(repo.getName());
            return StreamSupport.stream(array.spliterator(), true)
                    .map(element -> {
                        JsonObject object = element.getAsJsonObject();
                        String name = object.get("name").getAsString();
                        return labelService.getOrCreate(name.toLowerCase());
                    })
                    .collect(Collectors.toSet());
        } catch (NonTransientDataAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MetadataCrawlingException("Failed to retrieve repository labels", ex);
        }
    }

    private Set<GitRepoLanguage> retrieveRepoLanguages(GitRepo repo) {
        try {
            JsonObject object = gitHubApiConnector.fetchRepoLanguages(repo.getName());
            return object.entrySet().stream()
                    .map(entry -> {
                        Language language = languageService.getOrCreate(entry.getKey());
                        GitRepoLanguage.Key key = new GitRepoLanguage.Key(repo.getId(), language.getId());
                        return GitRepoLanguage.builder()
                                .key(key)
                                .repo(repo)
                                .language(language)
                                .sizeOfCode(entry.getValue().getAsLong())
                                .build();
                    })
                    .collect(Collectors.toSet());
        } catch (NonTransientDataAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MetadataCrawlingException("Failed to retrieve repository languages", ex);
        }
    }

    private Set<Topic> retrieveTopics(GitRepo repo) {
        try {
            JsonArray array = gitHubApiConnector.fetchRepoTopics(repo.getName());
            return StreamSupport.stream(array.spliterator(), true)
                    .map(entry -> topicService.getOrCreate(entry.getAsString()))
                    .collect(Collectors.toSet());
        } catch (NonTransientDataAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MetadataCrawlingException("Failed to retrieve repository topics", ex);
        }
    }
}
