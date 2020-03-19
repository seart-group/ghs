package com.dabico.gseapp.converter;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.GitRepoLabelDto;
import com.dabico.gseapp.dto.GitRepoLanguageDto;
import com.dabico.gseapp.github.GitHubPageCrawlerService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dabico.gseapp.util.DateUtils.fromGitDateString;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoConverter {
    public GitRepo jsonToGitRepo(JsonObject json) throws IOException {
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

    public List<GitRepoDto> repoListToRepoDtoList(List<GitRepo> repos){
        return repos.stream().map(this::repoToRepoDto).collect(Collectors.toList());
    }

    public List<GitRepoLabelDto> labelListToLabelDtoList(List<GitRepoLabel> labels){
        return labels.stream().map(this::labelToLabelDto).collect(Collectors.toList());
    }

    public List<GitRepoLanguageDto> languageListToLanguageDtoList(List<GitRepoLanguage> languages){
        return languages.stream().map(this::languageToLanguageDto).collect(Collectors.toList());
    }

    public GitRepoDto repoToRepoDto(GitRepo repo){
        return GitRepoDto.builder()
                .id(repo.getId())
                .name(repo.getName())
                .isFork(repo.getIsFork())
                .commits(repo.getCommits())
                .branches(repo.getBranches())
                .defaultBranch(repo.getDefaultBranch())
                .releases(repo.getReleases())
                .contributors(repo.getContributors())
                .license(repo.getLicense())
                .watchers(repo.getWatchers())
                .stargazers(repo.getStargazers())
                .forks(repo.getForks())
                .size(repo.getSize())
                .createdAt(repo.getCreatedAt())
                .pushedAt(repo.getPushedAt())
                .updatedAt(repo.getUpdatedAt())
                .homepage(repo.getHomepage())
                .mainLanguage(repo.getMainLanguage())
                .totalIssues(repo.getTotalIssues())
                .openIssues(repo.getOpenIssues())
                .totalPullRequests(repo.getTotalPullRequests())
                .openPullRequests(repo.getOpenPullRequests())
                .lastCommit(repo.getLastCommit())
                .lastCommitSHA(repo.getLastCommitSHA())
                .hasWiki(repo.getHasWiki())
                .isArchived(repo.getIsArchived())
                .build();
    }

    public GitRepoLabelDto labelToLabelDto(GitRepoLabel label){
        return GitRepoLabelDto.builder()
                              .id(label.getId())
                              .repoId(label.getRepo().getId())
                              .label(label.getLabel())
                              .build();
    }

    public GitRepoLanguageDto languageToLanguageDto(GitRepoLanguage language){
        return GitRepoLanguageDto.builder()
                                 .id(language.getId())
                                 .repoId(language.getRepo().getId())
                                 .language(language.getLanguage())
                                 .sizeOfCode(language.getSizeOfCode())
                                 .build();
    }
}
