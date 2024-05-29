package ch.usi.si.seart.job;

import ch.usi.si.seart.exception.MetadataCrawlingException;
import ch.usi.si.seart.exception.UnsplittableRangeException;
import ch.usi.si.seart.github.GitHubGraphQlConnector;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.License;
import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.model.join.GitRepoLanguage;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.service.LabelService;
import ch.usi.si.seart.service.LanguageService;
import ch.usi.si.seart.service.LicenseService;
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
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Job
@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(value = "ghs.crawler.enabled", havingValue = "true")
@ConditionalOnExpression(value = "not '${ghs.crawler.languages}'.blank and not '${ghs.github.tokens}'.blank")
public class CrawlProjectsJob implements Runnable {

    GitRepoService gitRepoService;
    TopicService topicService;
    LabelService labelService;
    LicenseService licenseService;
    LanguageService languageService;

    GitHubRestConnector gitHubRestConnector;
    GitHubGraphQlConnector gitHubGraphQlConnector;

    Ranges.Printer<Date> rangePrinter;
    Ranges.Splitter<Date> rangeSplitter;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void run() {
        log.info("Initializing language queue...");
        Collection<Language> languages = languageService.getTargetedLanguages();
        List<String> order = languages.stream()
                .map(Language::getName)
                .toList();
        log.info("Language crawling order: {}", order);
        for (Language language : languages) {
            Language.Progress progress = languageService.getProgress(language);
            new LanguageCrawler(language, progress).run();
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private class LanguageCrawler implements Runnable {

        Language language;

        Date start;

        Deque<Range<Date>> intervals;

        LanguageCrawler(Language language, Language.Progress progress) {
            this.language = language;
            this.start = progress.getCheckpoint();
            this.intervals = new SelfAdjustingDateRangeDequeue(progress.getCheckpoint());
        }

        private static class SelfAdjustingDateRangeDequeue extends ArrayDeque<Range<Date>> {

            SelfAdjustingDateRangeDequeue(Date date) {
                super(Collections.singleton(Range.closed(date, oneHourAgo())));
            }

            /*
             * Issue #145
             *
             * If this is the last range in the queue,
             * then re-adjust its lower endpoint,
             * as it may be behind the current time.
             * This is to make up for the fact that the range should
             * technically continuously expand as we mine.
             */
            @NotNull
            @Override
            public Range<Date> pop() {
                Range<Date> result = super.pop();
                return isEmpty() ? Ranges.closed(result.lowerEndpoint(), oneHourAgo()) : result;
            }

            private static Date oneHourAgo() {
                return Date.from(Instant.now().minus(Duration.ofHours(1)));
            }
        }

        @Override
        public void run() {
            String name = language.getName();
            log.info("Starting crawling {} repositories updated since: {}", name, start);
            do {
                int limit = 5;
                int size = intervals.size();
                log.info("Next crawl intervals:");
                intervals.stream()
                        .limit(limit)
                        .map(rangePrinter::print)
                        .forEach(string -> log.info("    [{}]", string));
                if (size > limit) log.info("    {} omitted ...", size - limit);
                mine(intervals.pop());
            } while (!intervals.isEmpty());
            log.info("Finished crawling {} repositories updated since: {}", name, start);
        }

        private void mine(Range<Date> range) {
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
                saveRetrievedResults(results, 1, totalResults);
                mineRemainingResults(range, totalPages);
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
                intervals.push(ranges.getRight());
                intervals.push(ranges.getLeft());
                return true;
            } catch (UnsplittableRangeException ure) {
                return false;
            }
        }

        private void mineRemainingResults(Range<Date> range, int pages) {
            if (pages > 1) {
                int page = 2;
                while (page <= pages) {
                    try {
                        JsonObject json = gitHubRestConnector.searchRepositories(language.getName(), range, page);
                        int totalResults = json.get("total_count").getAsInt();
                        JsonArray results = json.get("items").getAsJsonArray();
                        saveRetrievedResults(results, (page - 1) * 100 + 1, totalResults);
                        page++;
                    } catch (NonTransientDataAccessException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        log.error("Failed to retrieve the remaining repositories", ex);
                    }
                }
            }
        }

        private void saveRetrievedResults(JsonArray results, int lowerIndex, int total) {
            int upperIndex = lowerIndex + results.size() - 1;
            log.info(
                    "Crawling {} {} repositories ({} - {} | total: {})",
                    results.size(), language.getName(), lowerIndex, upperIndex, total
            );

            for (JsonElement element : results) {
                JsonObject result = element.getAsJsonObject();
                saveRetrievedRepo(result, lowerIndex, total);
                lowerIndex++;
            }
        }

        private void saveRetrievedRepo(JsonObject result, int lowerIndex, int total) {
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

            String action = gitRepo.getId() != null
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

                String homepage = !json.get("homepage").isJsonNull()
                        ? json.getAsJsonPrimitive("homepage")
                        .getAsString()
                        .trim()
                        : null;
                gitRepo.setHomepage(Strings.emptyToNull(homepage));

                License license = Optional.ofNullable(json.get("license"))
                        .filter(Predicate.not(JsonElement::isJsonNull))
                        .map(JsonElement::getAsJsonObject)
                        .map(object -> object.getAsJsonPrimitive("name").getAsString())
                        .map(string -> string.replace("\"", ""))
                        .filter(StringUtils::hasText)
                        .map(licenseService::getOrCreate)
                        .orElse(null);
                gitRepo.setLicense(license);

                Long forks = json.getAsJsonPrimitive("forks").getAsLong();
                gitRepo.setForks(forks);

                Boolean hasWiki = json.getAsJsonPrimitive("has_wiki").getAsBoolean();
                gitRepo.setHasWiki(hasWiki);
                Boolean isFork = json.getAsJsonPrimitive("is_fork").getAsBoolean();
                gitRepo.setIsFork(isFork);
                Boolean isArchived = json.getAsJsonPrimitive("is_archived").getAsBoolean();
                gitRepo.setIsArchived(isArchived);
                Boolean isDisabled = json.getAsJsonPrimitive("is_disabled").getAsBoolean();
                gitRepo.setIsDisabled(isDisabled);
                Boolean isLocked = json.getAsJsonPrimitive("is_locked").getAsBoolean();
                gitRepo.setIsLocked(isLocked);

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

                gitRepo.setMainLanguage(language);

                gitRepo = gitRepoService.createOrUpdate(gitRepo);

                Set<Label> labels = extractLabels(gitRepo, json);
                if (!labels.isEmpty())
                    log.debug("Adding:    {} labels.", labels.size());
                gitRepo.setLabels(labels);

                Set<GitRepoLanguage> languages = extractLanguages(gitRepo, json);
                if (!languages.isEmpty())
                    log.debug("Adding:    {} languages.", languages.size());
                gitRepo.setLanguages(languages);

                Set<Topic> topics = extractTopics(gitRepo, json);
                if (!topics.isEmpty())
                    log.debug("Adding:    {} topics.", topics.size());
                gitRepo.setTopics(topics);

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

        private Set<Label> extractLabels(GitRepo gitRepo, JsonObject json) {
            JsonObject labels = json.getAsJsonObject("labels");
            long count = labels.getAsJsonPrimitive("count").getAsLong();
            if (count > 100) return retrieveLabels(gitRepo);
            return StreamSupport.stream(labels.getAsJsonArray("items").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(label -> label.getAsJsonPrimitive("name").getAsString())
                    .map(labelService::getOrCreate)
                    .collect(Collectors.toSet());
        }

        private Set<Label> retrieveLabels(GitRepo gitRepo) {
            try {
                JsonArray array = gitHubRestConnector.getRepositoryLabels(gitRepo.getName());
                return StreamSupport.stream(array.spliterator(), false)
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

        private Set<GitRepoLanguage> extractLanguages(GitRepo gitRepo, JsonObject json) {
            JsonObject languages = json.getAsJsonObject("languages");
            long count = languages.getAsJsonPrimitive("count").getAsLong();
            if (count > 100) return retrieveLanguages(gitRepo);
            return StreamSupport.stream(languages.getAsJsonArray("items").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(item -> {
                        long sizeOfCode = item.getAsJsonPrimitive("size").getAsLong();
                        String name = item.getAsJsonObject("node")
                                .getAsJsonPrimitive("name")
                                .getAsString();
                        Language language = languageService.getOrCreate(name);
                        GitRepoLanguage.Key key = new GitRepoLanguage.Key(gitRepo.getId(), language.getId());
                        return GitRepoLanguage.builder()
                                .key(key)
                                .repo(gitRepo)
                                .language(language)
                                .sizeOfCode(sizeOfCode)
                                .build();
                    })
                    .collect(Collectors.toSet());
        }

        private Set<GitRepoLanguage> retrieveLanguages(GitRepo gitRepo) {
            try {
                JsonObject object = gitHubRestConnector.getRepositoryLanguages(gitRepo.getName());
                return object.entrySet().stream()
                        .map(entry -> {
                            Language language = languageService.getOrCreate(entry.getKey());
                            GitRepoLanguage.Key key = new GitRepoLanguage.Key(gitRepo.getId(), language.getId());
                            return GitRepoLanguage.builder()
                                    .key(key)
                                    .repo(gitRepo)
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

        private Set<Topic> extractTopics(GitRepo gitRepo, JsonObject json) {
            JsonObject topics = json.getAsJsonObject("topics");
            long count = topics.getAsJsonPrimitive("count").getAsLong();
            if (count > 100) return retrieveTopics(gitRepo);
            return StreamSupport.stream(topics.getAsJsonArray("items").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(item -> item.getAsJsonObject("topic"))
                    .map(topic -> topic.getAsJsonPrimitive("name").getAsString())
                    .map(topicService::getOrCreate)
                    .collect(Collectors.toSet());
        }

        private Set<Topic> retrieveTopics(GitRepo gitRepo) {
            try {
                JsonArray array = gitHubRestConnector.getRepositoryTopics(gitRepo.getName());
                return StreamSupport.stream(array.spliterator(), false)
                        .map(entry -> topicService.getOrCreate(entry.getAsString()))
                        .collect(Collectors.toSet());
            } catch (NonTransientDataAccessException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new MetadataCrawlingException("Failed to retrieve repository topics", ex);
            }
        }
    }
}
