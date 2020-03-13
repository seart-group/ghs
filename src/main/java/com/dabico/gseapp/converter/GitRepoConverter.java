package com.dabico.gseapp.converter;

import com.dabico.gseapp.github.GitHubPageCrawlerService;
import com.dabico.gseapp.model.GitRepo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

import static com.dabico.gseapp.util.DateUtils.fromGitDateString;

public class GitRepoConverter {
    public static GitRepo jsonToGitRepo(JsonObject json) throws IOException {
        String repositoryURL = json.get("html_url").getAsString();
        GitHubPageCrawlerService crawlerService = new GitHubPageCrawlerService(repositoryURL);
        crawlerService.mine();
        JsonElement license = json.get("license");
        JsonElement homepage = json.get("homepage");
        return GitRepo.builder()
                      .name(json.get("full_name").getAsString())
                      .isFork(json.get("fork").getAsBoolean())
                      .commits(crawlerService.getCommits())
                      .branches(crawlerService.getBranches())
                      .defaultBranch(json.get("default_branch").getAsString())
                      .releases(crawlerService.getReleases())
                      .contributors(crawlerService.getContributors())
                      .license((license.isJsonNull()) ? null : license.getAsJsonObject().get("name").getAsString())
                      .watchers(crawlerService.getWatchers())
                      .stargazers(crawlerService.getStars())
                      .forks(json.get("forks_count").getAsLong())
                      .size(json.get("size").getAsLong())
                      .createdAt(fromGitDateString(json.get("created_at").getAsString()))
                      .pushedAt(fromGitDateString(json.get("pushed_at").getAsString()))
                      .updatedAt(fromGitDateString(json.get("updated_at").getAsString()))
                      .homepage(homepage.isJsonNull() ? null : homepage.getAsString())
                      .mainLanguage(json.get("language").getAsString())
                      .totalIssues(crawlerService.getTotalIssues())
                      .openIssues(crawlerService.getOpenIssues())
                      .totalPullRequests(crawlerService.getTotalPullRequests())
                      .openPullRequests(crawlerService.getOpenPullRequests())
                      .lastCommit(crawlerService.getLastCommit())
                      .lastCommitSHA(crawlerService.getLastCommitSHA())
                      .hasWiki(json.get("has_wiki").getAsBoolean())
                      .isArchived(json.get("archived").getAsBoolean())
                      .build();
    }
}
