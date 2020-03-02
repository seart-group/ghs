package com.dabico.gseapp.github;

import lombok.Getter;
import okhttp3.Response;

@Getter
public class GitHubPageCrawlerService {
    private Response projectPage;
    private Response issuesPage;
    private Response pullsPage;
    private Response commitsPage;

    public GitHubPageCrawlerService(String url){
        WebpageCrawlerService crawlerService = new WebpageCrawlerService();
        this.projectPage = crawlerService.crawlPage(url);
        this.issuesPage = crawlerService.crawlPage(url+"/issues");
        this.pullsPage = crawlerService.crawlPage(url+"/pulls");
        this.commitsPage = crawlerService.crawlPage(url+"/commits");
    }
}
