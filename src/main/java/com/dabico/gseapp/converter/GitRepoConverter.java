package com.dabico.gseapp.converter;

import com.dabico.gseapp.github.GitHubPageCrawlerService;
import com.dabico.gseapp.model.GitRepo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

public class GitRepoConverter {
    public static GitRepo jsonToGitRepo(JsonObject json) throws IOException {
        String repositoryURL = json.get("html_url").getAsString();
        GitHubPageCrawlerService crawlerService = new GitHubPageCrawlerService(repositoryURL);
        crawlerService.mine();
        //TODO Pass parser results to builder
        JsonElement license = json.get("license");
        return GitRepo.builder()
                      .name(json.get("full_name").getAsString())
                      .isFork(json.get("fork").getAsBoolean())
                      //commits - main project page
                      //branches - main project page
                      .defaultBranch(json.get("default_branch").getAsString())
                      //releases - main project page
                      //contributors - main project page
                      .license((license.isJsonNull()) ? null : license.getAsJsonObject().get("name").getAsString())
                      //watchers - main project page
                      .stargazers(json.get("stargazers_count").getAsLong())
                      .forks(json.get("forks_count").getAsLong())
                      .size(json.get("size").getAsLong())
                      .mainLanguage(json.get("language").getAsString())
                      //total issues - issues page
                      //open issues - issues page
                      //total pull requests - pulls page
                      //open pull requests - pulls page
                      //last commit - commits page
                      //last commit sha - commits page
                      .hasWiki(json.get("has_wiki").getAsBoolean())
                      .isArchived(json.get("archived").getAsBoolean())
                      .build();
    }
}
