package com.dabico.gseapp.github_service;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RepoHtmlPageExtraInfo {
    Long watchers = null;
    Long commits = null;
    Long branches = null;
    Long releases = null;
    Long contributors= null;

    Long totalIssues = null;
    Long openIssues = null;
    Long totalPullRequests = null;
    Long openPullRequests = null;

    Date lastCommit = null;
    String lastCommitSHA = null;

}
