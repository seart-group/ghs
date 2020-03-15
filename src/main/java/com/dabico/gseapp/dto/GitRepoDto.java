package com.dabico.gseapp.dto;

import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
public class GitRepoDto {
    Long id;
    String name;
    Boolean isFork;
    Long commits;
    Long branches;
    String defaultBranch;
    Long releases;
    Long contributors;
    String license;
    Long watchers;
    Long stargazers;
    Long forks;
    Long size;
    Date createdAt;
    Date pushedAt;
    Date updatedAt;
    String homepage;
    String mainLanguage;
    Long totalIssues;
    Long openIssues;
    Long totalPullRequests;
    Long openPullRequests;
    Date lastCommit;
    String lastCommitSHA;
    Boolean hasWiki;
    Boolean isArchived;
    @Builder.Default
    Set<String> languages = new HashSet<>();
    @Builder.Default
    Set<String> labels = new HashSet<>();
}
