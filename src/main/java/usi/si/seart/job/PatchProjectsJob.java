package usi.si.seart.job;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.github.GitHubAPIConnector;
import usi.si.seart.model.GitRepo;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.stereotype.Job;
import usi.si.seart.util.Dates;
import usi.si.seart.util.Optionals;

import java.util.Date;

@Job
@Slf4j
@ConditionalOnExpression(value = "not '${app.crawl.tokens}'.isBlank()")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatchProjectsJob {

    ApplicationContext applicationContext;

    GitRepoService gitRepoService;

    GitHubAPIConnector gitHubApiConnector;

    @Autowired
    public PatchProjectsJob(ApplicationContext applicationContext, GitRepoService gitRepoService, GitHubAPIConnector gitHubApiConnector) {
        this.applicationContext = applicationContext;
        this.gitRepoService = gitRepoService;
        this.gitHubApiConnector = gitHubApiConnector;
    }

    @Transactional(readOnly = true)
    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void run() {
        log.info(
                "Started patching on {}/{} repositories",
                gitRepoService.countPatchCandidates(),
                gitRepoService.count()
        );
        PatchProjectsJob self = applicationContext.getBean(getClass());
        gitRepoService.streamPatchCandidates().forEach(self::patch);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void patch(Pair<Long, String> identifiers) {
        Long id = identifiers.getFirst();
        String name = identifiers.getSecond();
        Optionals.ofThrowable(() -> gitRepoService.getById(id)).ifPresentOrElse(
                this::patch,
                () -> log.debug("Skipping:  {} [{}]", name, id)
        );
    }

    @SuppressWarnings("DuplicatedCode")
    private void patch(@NotNull GitRepo gitRepo) {
        Long id = gitRepo.getId();
        String name = gitRepo.getName();
        log.debug("Patching:  {} [{}]", name, id);
        try {
            JsonObject json = gitHubApiConnector.fetchRepoInfo(name);

            Long branches = json.getAsJsonObject("branches")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setBranches(branches);

            Long releases = json.getAsJsonObject("releases")
                    .getAsJsonPrimitive("count")
                    .getAsLong();
            gitRepo.setReleases(releases);

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

            if (gitRepo.getContributors() == null) {
                Long contributors = gitHubApiConnector.fetchNumberOfContributors(name);
                gitRepo.setContributors(contributors);
            }

            JsonElement defaultBranch = json.get("default_branch");
            if (!defaultBranch.isJsonNull()) {
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
        } catch (Exception ex) {
            log.error("Failed:    {} [{}]", name, id, ex);
        } finally {
            gitRepoService.createOrUpdate(gitRepo);
        }
    }
}
