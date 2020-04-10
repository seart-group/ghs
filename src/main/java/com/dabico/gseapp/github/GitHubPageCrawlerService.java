package com.dabico.gseapp.github;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;

import static java.lang.Long.parseLong;
import static com.dabico.gseapp.util.DateUtils.fromGitDateString;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitHubPageCrawlerService {
    static final Logger logger = LoggerFactory.getLogger(GitHubPageCrawlerService.class);

    static String commitsReg          = "#js-repo-pjax-container > div > div > div > ul > li:nth-child(1) > a > span";
    static String commitsAlt          = "#js-repo-pjax-container > div.container-lg.clearfix.new-discussion-timeline.px-3 > div > div.overall-summary.border-bottom-0.mb-0.rounded-bottom-0 > ul > li.commits > a > span";
    static String branchesReg         = "#js-repo-pjax-container > div > div > div > ul > li:nth-child(2) > a > span";
    static String branchesAlt         = "#js-repo-pjax-container > div.container-lg.clearfix.new-discussion-timeline.px-3 > div > div.overall-summary.border-bottom-0.mb-0.rounded-bottom-0 > ul > li:nth-child(2) > a > span";
    static String releasesReg         = "#js-repo-pjax-container > div > div > div > ul > li:nth-child(4) > a > span";
    static String releasesAlt         = "#js-repo-pjax-container > div.container-lg.clearfix.new-discussion-timeline.px-3 > div > div.overall-summary.border-bottom-0.mb-0.rounded-bottom-0 > ul > li:nth-child(4) > a > span";
    static String contributorsReg     = "#js-repo-pjax-container > div > div > div > ul > li:nth-child(5) > a > span";
    static String contributorsAlt     = "#js-repo-pjax-container > div.container-lg.clearfix.new-discussion-timeline.px-3 > div > div.overall-summary.border-bottom-0.mb-0.rounded-bottom-0 > ul > li:nth-child(5) > a > span";
    static String watchersReg         = "#js-repo-pjax-container > div > div > ul > li:nth-child(1) > a:nth-child(2)";
    static String watchersAlt         = "#js-repo-pjax-container > div > div > ul > li:nth-child(2) > a:nth-child(2)";
    static String watchersSeleniumReg = "#js-repo-pjax-container > div.pagehead.repohead.hx_repohead.readability-menu.bg-gray-light.pb-0.pt-3 > div > ul > li:nth-child(1) > a:nth-child(2)";
    static String watchersSeleniumAlt = "#js-repo-pjax-container > div.pagehead.repohead.hx_repohead.readability-menu.bg-gray-light.pb-0.pt-3 > div > ul > li:nth-child(2) > a:nth-child(2)";
    static String openReg             = "#js-issues-toolbar > div > div > div > a:nth-child(1)";
    static String closedReg           = "#js-issues-toolbar > div > div > div > a:nth-child(2)";
    static String commitLinkReg       = "#js-repo-pjax-container > div > div > div > ol:nth-child(2) > li > div > div > a";
    static String commitDateReg       = "#js-repo-pjax-container > div > div > div > div > div > relative-time";
    static String commitSHAReg        = "#js-repo-pjax-container > div > div > div > div > div > span:nth-child(2) > span";

    final String repoURL;
    final ChromeDriver driver;
    final WebDriverWait wait;
    long commits = 0;
    long branches = 0;
    long releases = 0;
    long contributors = 0;
    long watchers = 0;
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
        Document document = Jsoup.connect(repoURL).userAgent("Mozilla").followRedirects(false).get();
        try {
            commits  = parseLong(normalizeNumberString(document.select(commitsReg).first().html()));
        } catch (NullPointerException ignored) {
            commits  = mineCommitsSelenium();
        }

        try {
            branches = parseLong(normalizeNumberString(document.select(branchesReg).first().html()));
        } catch (NullPointerException ex){
            branches = mineBranchesSelenium();
        }

        try {
            releases = parseLong(normalizeNumberString(document.select(releasesReg).first().html()));
        } catch (NullPointerException ex){
            releases = mineReleasesSelenium();
        }

        try {
            contributors = parseLong(normalizeNumberString(document.select(contributorsReg).first().html()));
        } catch (NullPointerException ex){
            contributors = mineContributorsSelenium();
        }

        try {
            watchers = parseLong(normalizeNumberString(document.select(watchersReg).first().attr("aria-label").split(" ")[0]));
        } catch (NullPointerException ex1){
            try {
                watchers = parseLong(normalizeNumberString(document.select(watchersAlt).first().attr("aria-label").split(" ")[0]));
            } catch (NullPointerException ex2){
                watchers = mineWatchersSelenium();
            }
        }
    }

    private void mineIssuesPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/issues").userAgent("Mozilla").followRedirects(false).get();
        try {
            openIssues  = parseLong(normalizeNumberString(document.select(openReg).first().text().split(" ")[0]));
            totalIssues = openIssues + parseLong(normalizeNumberString(document.select(closedReg).first().text().split(" ")[0]));
        } catch (NullPointerException ignored) {}
    }

    private void minePullsPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/pulls").userAgent("Mozilla").followRedirects(false).get();
        try {
            openPullRequests  = parseLong(normalizeNumberString(document.select(openReg).first().text().split(" ")[0]));
            totalPullRequests = openPullRequests + parseLong(normalizeNumberString(document.select(closedReg).first().text().split(" ")[0]));
        } catch (NullPointerException ignored) {}
    }

    private void mineCommitsPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/commits").userAgent("Mozilla").followRedirects(false).get();
        String link = document.select(commitLinkReg).first().attr("href");
        document = Jsoup.connect(Endpoints.DEFAULT.getUrl()+"/"+link).userAgent("Mozilla").followRedirects(false).get();
        lastCommit = fromGitDateString(document.select(commitDateReg).first().attr("datetime"));
        lastCommitSHA = document.select(commitSHAReg).first().text();
    }

    private long mineCommitsSelenium() {
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(commitsReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(commitsReg).getText()));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(commitsAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(commitsAlt).getText()));
        }
    }

    private long mineContributorsSelenium() {
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(contributorsReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(contributorsReg).getText()));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(contributorsAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(contributorsAlt).getText()));
        }
    }

    private long mineBranchesSelenium(){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(branchesReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(branchesReg).getText()));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(branchesAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(branchesAlt).getText()));
        }
    }

    private long mineReleasesSelenium(){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(releasesReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(releasesReg).getText()));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(releasesAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(releasesAlt).getText()));
        }
    }

    private long mineWatchersSelenium(){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(watchersSeleniumReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(watchersSeleniumReg).getAttribute("aria-label").split(" ")[0]));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(watchersSeleniumAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(watchersSeleniumAlt).getAttribute("aria-label").split(" ")[0]));
        }
    }

    private String normalizeNumberString(String input){ return input.trim().replaceAll(",",""); }
}
