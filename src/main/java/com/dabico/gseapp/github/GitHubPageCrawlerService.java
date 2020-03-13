package com.dabico.gseapp.github;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Long.parseLong;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GitHubPageCrawlerService {
    static final Logger logger = LoggerFactory.getLogger(GitHubPageCrawlerService.class);

    final String repoURL;
    long commits = 0;
    long branches = 0;
    long releases = 0;
    long contributors = 0;
    long watchers = 0;
    long stars = 0;
    long totalIssues = 0;
    long openIssues = 0;
    long totalPullRequests = 0;
    long openPullRequests = 0;
    Date lastCommit = null;
    String lastCommitSHA = null;

    public void mine() throws IOException {
        logger.info("Mining data for: " + repoURL);
        mineProjectPage();
        mineIssuesPage();
        minePullsPage();
        mineCommitsPage();
    }

    private void mineProjectPage() throws IOException {
        Document document   = Jsoup.connect(repoURL).get();
        Elements summary_ul = document.getElementsByClass("numbers-summary");
        commits  = parseLong(normalizeNumberString(summary_ul.get(0).childNode(1).childNode(1).childNode(3).childNode(0).toString()));
        branches = parseLong(normalizeNumberString(summary_ul.get(0).childNode(3).childNode(1).childNode(3).childNode(0).toString()));
        releases = parseLong(normalizeNumberString(summary_ul.get(0).childNode(7).childNode(1).childNode(3).childNode(0).toString()));
        //TODO fetching contributors can sometimes be unpredictable as they might not load on time
        //contributors = parseLong(normalizeNumberString(summary_ul.get(0).childNode(9).childNode(1).childNode(3).childNode(0).toString()));
        Elements pagehead_ul = document.getElementsByClass("pagehead-actions flex-shrink-0 ");
        watchers = parseLong(normalizeNumberString(pagehead_ul.get(0).childNode(3).childNode(3).attr("aria-label").split(" ")[0]));
        stars    = parseLong(normalizeNumberString(pagehead_ul.get(0).childNode(5).childNode(3).attr("aria-label").split(" ")[0]));
    }

    private void mineIssuesPage() throws IOException {
        Document document  = Jsoup.connect(repoURL + "/issues").get();
        Elements table_div = document.getElementsByClass("table-list-header-toggle states flex-auto pl-0");
        long open   = parseLong(normalizeNumberString(table_div.get(0).childNode(1).childNode(2).toString().trim().split(" ")[0]));
        long closed = parseLong(normalizeNumberString(table_div.get(0).childNode(3).childNode(2).toString().trim().split(" ")[0]));
        openIssues  = open;
        totalIssues = open + closed;
    }

    private void minePullsPage() throws IOException {
        Document document  = Jsoup.connect(repoURL + "/pulls").get();
        Elements table_div = document.getElementsByClass("table-list-header-toggle states flex-auto pl-0");
        long open   = parseLong(normalizeNumberString(table_div.get(0).childNode(1).childNode(2).toString().trim().split(" ")[0]));
        long closed = parseLong(normalizeNumberString(table_div.get(0).childNode(3).childNode(2).toString().trim().split(" ")[0]));
        openPullRequests  = open;
        totalPullRequests = open + closed;
    }

    private void mineCommitsPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/commits").get();
        Elements button_a = document.getElementsByClass("sha btn btn-outline BtnGroup-item");
        String link = button_a.get(0).attr("href");
        document = Jsoup.connect(Endpoints.DEFAULT.getUrl()+"/"+link).get();
        String date_time = document.getElementsByTag("relative-time").attr("datetime");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            lastCommit = dateFormat.parse(date_time);
        } catch (ParseException ignored){}
        lastCommitSHA = document.getElementsByClass("sha user-select-contain").get(0).childNode(0).toString();
    }

    private String normalizeNumberString(String input){ return input.trim().replaceAll(",",""); }
}
