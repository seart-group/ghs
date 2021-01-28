package usi.si.seart.gseapp.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
    @JacksonXmlElementWrapper(localName = "GitRepoLanguages")
    @JacksonXmlProperty(localName = "GitRepoLanguage")
    List<GitRepoLanguageDto> languages = new ArrayList<>();
    @Builder.Default
    @JacksonXmlElementWrapper(localName = "GitRepoLabels")
    @JacksonXmlProperty(localName = "GitRepoLabel")
    List<GitRepoLabelDto> labels = new ArrayList<>();
}
