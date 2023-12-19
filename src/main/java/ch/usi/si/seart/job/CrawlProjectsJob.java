package ch.usi.si.seart.job;

import ch.usi.si.seart.config.properties.CrawlerProperties;
import ch.usi.si.seart.exception.MetadataCrawlingException;
import ch.usi.si.seart.exception.UnsplittableRangeException;
import ch.usi.si.seart.github.GitHubGraphQlConnector;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.model.join.GitRepoLanguage;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.service.LabelService;
import ch.usi.si.seart.service.LanguageService;
import ch.usi.si.seart.service.TopicService;
import ch.usi.si.seart.stereotype.Job;
import ch.usi.si.seart.util.Dates;
import ch.usi.si.seart.util.Optionals;
import ch.usi.si.seart.util.Ranges;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Job
@Slf4j
@DependsOn("languageInitializationBean")
@ConditionalOnProperty(value = "ghs.crawler.enabled", havingValue = "true")
@ConditionalOnExpression(value = "not '${ghs.crawler.languages}'.blank and not '${ghs.github.tokens}'.blank")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlProjectsJob implements Runnable {

    Deque<Range<Date>> requestQueue = new ArrayDeque<>();

    GitRepoService gitRepoService;
    TopicService topicService;
    LabelService labelService;
    LanguageService languageService;

    GitHubRestConnector gitHubRestConnector;
    GitHubGraphQlConnector gitHubGraphQlConnector;

    CrawlerProperties crawlerProperties;

    Ranges.Printer<Date> rangePrinter;
    Ranges.Splitter<Date> rangeSplitter;

    @Scheduled(fixedDelayString = "${ghs.crawler.delay-between-runs}")
    public void run() {
        log.info("Initializing language queue...");
        Collection<Language> languages = languageService.getTargetedLanguages();
        List<String> order = languages.stream()
                .map(Language::getName)
                .toList();
        log.info("Language crawling order: {}", order);
        for (Language language : languages) {
            requestQueue.clear();
            Language.Progress progress = languageService.getProgress(language);
            Date lower = progress.getCheckpoint();
            Date upper = Date.from(Instant.now().minus(Duration.ofHours(1)));
            Range<Date> dateRange = Ranges.closed(lower, upper);
            crawlRepositories(dateRange, language);
        }
        Duration delayBetweenRuns = crawlerProperties.getDelayBetweenRuns();
        Instant nextRun = Instant.now().plus(delayBetweenRuns);
        log.info("Next crawl scheduled for: {}", Date.from(nextRun));
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
                    .map(rangePrinter::print)
                    .forEach(string -> log.info("    [{}]", string));
            if (size > limit) log.info("    {} omitted ...", size - limit);
            Range<Date> first = requestQueue.pop();
            retrieveRepositories(first, language);
        } while (!requestQueue.isEmpty());
        log.info("Finished crawling {} repositories updated through: {}", language.getName(), range);
    }

    private void retrieveRepositories(Range<Date> range, Language language) {
        if (requestQueue.isEmpty()) {
            /*
             * Issue #145
             *
             * If this is the last range in the request queue,
             * re-adjust it as it may be behind the current time.
             * This is to make up for the fact that the range should
             * technically continuously expand as we mine.
             * I don't really like this code, but I guess
             * this is the fastest solution to the problem.
             */
            Date lower = range.lowerEndpoint();
            Date upper = Date.from(Instant.now().minus(Duration.ofHours(1)));
            range = Ranges.closed(lower, upper);
        }
        String name = language.getName();
        int page = 1;
        try {
            JsonObject json = gitHubRestConnector.searchRepositories(name, range, page);
            int totalResults = json.get("total_count").getAsInt();
            int totalPages = (int) Math.ceil(totalResults / 100.0);
            if (totalResults == 0) return;
            log.info("Retrieved results: " + totalResults);
            if (totalResults > 1000) {
                boolean splittable = splitAndEnqueue(range);
                if (splittable) return;
                String value = rangePrinter.print(range);
                log.warn("Encountered range that could not be further split [{}]!", value);
                log.info("Proceeding with mining anyway to mitigate data loss...");
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

    private boolean splitAndEnqueue(Range<Date> range) {
        try {
            Pair<Range<Date>, Range<Date>> ranges = rangeSplitter.split(range);
            requestQueue.push(ranges.getRight());
            requestQueue.push(ranges.getLeft());
            return true;
        } catch (UnsplittableRangeException ure) {
            return false;
        }
    }

    private void retrieveRemainingRepos(Range<Date> range, String language, int pages) {
        if (pages > 1) {
            int page = 2;
            while (page <= pages) {
                try {
                    JsonObject json = gitHubRestConnector.searchRepositories(language, range, page);
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

    private void saveRetrievedRepos(JsonArray results, String language, int lowerIndex, int total) {
        int upperIndex = lowerIndex + results.size() - 1;
        log.info(
                "Crawling {} {} repositories ({} - {} | total: {})",
                results.size(), language, lowerIndex, upperIndex, total
        );

        for (JsonElement element : results) {
            JsonObject result = element.getAsJsonObject();
            saveRetrievedRepo(result, language, lowerIndex, total);
            lowerIndex++;
        }
    }

    private void saveRetrievedRepo(JsonObject result, String language, int lowerIndex, int total) {
        String name = result.getAsJsonPrimitive("full_name").getAsString();
        Optional<GitRepo> optional = Optionals.ofThrowable(() -> gitRepoService.getByName(name));
        GitRepo gitRepo = optional.orElseGet(() -> GitRepo.builder().name(name).build());

        Date createdAt = Dates.fromGitDateString(result.getAsJsonPrimitive("created_at").getAsString());
        Date updatedAt = Dates.fromGitDateString(result.getAsJsonPrimitive("updated_at").getAsString());
        Date pushedAt = Dates.fromGitDateString(result.getAsJsonPrimitive("pushed_at").getAsString());

        if (shouldSkip(gitRepo, updatedAt, pushedAt)) {
            log.info("Skipping:  {} [{}/{}]", name, lowerIndex, total);
            log.debug("Updated:   {}", updatedAt);
            log.debug("Pushed:    {}", pushedAt);
            return;
        }

        String action = (gitRepo.getId() != null)
                ? "Updating:  "
                : "Saving:    ";
        log.info("{}{} [{}/{}]", action, name, lowerIndex, total);

        gitRepo.setCreatedAt(createdAt);
        gitRepo.setPushedAt(pushedAt);
        gitRepo.setUpdatedAt(updatedAt);

        try {
            JsonObject json = gitHubGraphQlConnector.getRepository(name);

            Long size = json.getAsJsonPrimitive("size").getAsLong();
            gitRepo.setSize(size);

            String homepage = (!json.get("homepage").isJsonNull())
                    ? json.getAsJsonPrimitive("homepage")
                    .getAsString()
                    .trim()
                    : null;
            gitRepo.setHomepage(Strings.emptyToNull(homepage));

            String license = (!json.get("license").isJsonNull())
                    ? json.getAsJsonObject("license")
                    .getAsJsonPrimitive("name")
                    .getAsString()
                    .replace("\"", "")
                    : null;
            gitRepo.setLicense(license);

            Long forks = json.getAsJsonPrimitive("forks").getAsLong();
            gitRepo.setForks(forks);

            Boolean hasWiki = json.getAsJsonPrimitive("has_wiki").getAsBoolean();
            gitRepo.setHasWiki(hasWiki);
            Boolean isFork = json.getAsJsonPrimitive("is_fork").getAsBoolean();
            gitRepo.setIsFork(isFork);
            Boolean isArchived = json.getAsJsonPrimitive("is_archived").getAsBoolean();
            gitRepo.setIsArchived(isArchived);

            Long stargazers = json.getAsJsonObject("stars")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setStargazers(stargazers);

            Long branches = json.getAsJsonObject("branches")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setBranches(branches);

            Long releases = json.getAsJsonObject("releases")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setReleases(releases);

            Long watchers = json.getAsJsonObject("watchers")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setWatchers(watchers);

            Long totalPullRequests = json.getAsJsonObject("total_pull_requests")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setTotalPullRequests(totalPullRequests);

            Long openPullRequests = json.getAsJsonObject("open_pull_requests")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setOpenPullRequests(openPullRequests);

            Long totalIssues = json.getAsJsonObject("total_issues")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setTotalIssues(totalIssues);

            Long openIssues = json.getAsJsonObject("open_issues")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setOpenIssues(openIssues);

            JsonElement defaultBranch = json.get("default_branch");
            if (!defaultBranch.isJsonNull()) {
                /*
                 * This can technically happen for uninitialized repositories
                 * (e.g. https://github.com/dabico/dl4se-empty).
                 * While these should typically never be encountered while mining,
                 * it's better to be safe than sorry...
                 */
                String branchName = defaultBranch.getAsJsonObject()
                        .getAsJsonPrimitive("name")
                        .getAsString();
                gitRepo.setDefaultBranch(branchName);
                JsonObject history = defaultBranch.getAsJsonObject()
                        .getAsJsonObject("history");
                Long commits = history.getAsJsonObject("commits")
                        .getAsJsonPrimitive("count")
                        .getAsLong();
                gitRepo.setCommits(commits);
                JsonObject commit = history.getAsJsonObject("commits")
                        .getAsJsonArray("items")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("commit");
                Date lastCommit = Dates.fromGitDateString(commit.getAsJsonPrimitive("date").getAsString());
                gitRepo.setLastCommit(lastCommit);
                String lastCommitSHA = commit.getAsJsonPrimitive("sha").getAsString();
                gitRepo.setLastCommitSHA(lastCommitSHA);
            } else {
                gitRepo.setCommits(0L);
            }

            // Not available on GraphQL, so we have to keep using the page hack
            Long contributors = gitHubRestConnector.countRepositoryContributors(name);
            gitRepo.setContributors(contributors);

            Language mainLanguage = languageService.getOrCreate(language);
            gitRepo.setMainLanguage(mainLanguage);

            gitRepo = gitRepoService.createOrUpdate(gitRepo);

            long labels = json.getAsJsonObject("labels")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            if (labels > 0) {
                log.debug("Adding:    {} labels.", labels);
                gitRepo.setLabels(retrieveRepoLabels(gitRepo));
            }

            long languages = json.getAsJsonObject("languages")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            if (languages > 0) {
                log.debug("Adding:    {} languages.", languages);
                gitRepo.setLanguages(retrieveRepoLanguages(gitRepo));
            }

            long topics = json.getAsJsonObject("topics")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            if (topics > 0) {
                log.debug("Adding:    {} topics.", topics);
                gitRepo.setTopics(retrieveTopics(gitRepo));
            }

            gitRepoService.createOrUpdate(gitRepo);
        } catch (NonTransientDataAccessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to save repository", ex);
        }
    }

    /*
     * Optimization for cases when crawler restarts
     * mid-interval and encounters unchanged information.
     */
    private boolean shouldSkip(GitRepo gitRepo, Date apiUpdatedAt, Date apiPushedAt) {
        Date dbUpdatedAt = gitRepo.getUpdatedAt();
        Date dbPushedAt = gitRepo.getPushedAt();
        boolean dbHasData = dbUpdatedAt != null && dbPushedAt != null;
        return dbHasData
                && dbUpdatedAt.compareTo(apiUpdatedAt) == 0
                && dbPushedAt.compareTo(apiPushedAt) == 0;
    }

    private Set<Label> retrieveRepoLabels(GitRepo repo) {
        try {
            JsonArray array = gitHubRestConnector.getRepositoryLabels(repo.getName());
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
            JsonObject object = gitHubRestConnector.getRepositoryLanguages(repo.getName());
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
            JsonArray array = gitHubRestConnector.getRepositoryTopics(repo.getName());
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
