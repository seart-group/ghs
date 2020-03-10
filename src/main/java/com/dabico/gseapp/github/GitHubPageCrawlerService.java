package com.dabico.gseapp.github;

import lombok.Getter;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Date;

@Getter
public class GitHubPageCrawlerService extends HTTPService {
    private final String repoURL;
    private int commits;
    private int branches;
    private int releases;
    private int contributors;
    private int watchers;
    private int totalIssues;
    private int openIssues;
    private int totalPullRequests;
    private int openPullRequests;
    private Date lastCommit;
    private String lastCommitSHA;


    public GitHubPageCrawlerService(String repoUrl) {
        super();
        this.repoURL = repoUrl;
    }

    public void mine() throws IOException {
        mineProjectPage();
        mineIssuesPage();
        minePullsPage();
        mineCommitsPage();
    }

    private void mineProjectPage() throws IOException {
        String projectRaw = crawlGitPage(this.repoURL);
        //TODO Extract results
    }

    private void mineIssuesPage() throws IOException {
        String issuesRaw = crawlGitPage(this.repoURL+"/issues");
        //TODO Extract results
    }

    private void minePullsPage() throws IOException {
        String pullsRaw = crawlGitPage(this.repoURL+"/pulls");
        //TODO Extract results
    }

    private void mineCommitsPage() throws IOException {
        String commitsRaw = crawlGitPage(this.repoURL+"/commits");
        //TODO Extract results
    }

    private String crawlGitPage(String url) throws IOException {
        Response response = getPageAsHTML(url);
        ResponseBody responseBody = response.body();
        String responseBodyString = null;
        if (responseBody != null){
            responseBodyString = responseBody.string();
        }
        response.close();
        return responseBodyString;
    }
}
