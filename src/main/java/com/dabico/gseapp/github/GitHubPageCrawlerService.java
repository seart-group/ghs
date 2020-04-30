package com.dabico.gseapp.github;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
    static String commitDateReg       = "#js-repo-pjax-container > div > div > div > ol:nth-child(2) > li > div > div:nth-child(2) > div:nth-child(2) > relative-time";
    static String commitSHAReg        = "#js-repo-pjax-container > div > div > div > ol > li > div > div > clipboard-copy";

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
        } catch (NumberFormatException ex){
            contributors = Long.MAX_VALUE;
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
        try {
            lastCommit = fromGitDateString(document.select(commitDateReg).first().attr("datetime"));
            lastCommitSHA = document.select(commitSHAReg).attr("value");
        } catch (NullPointerException ignored) {
            logger.error("Error locating commits");
        }
    }

    private long mineCommitsSelenium() {
        try {
            return mineWithSelenium(commitsReg,commitsAlt);
        } catch (TimeoutException ex) {
            logger.error("Number of commits could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }
    }

    private long mineContributorsSelenium() {
        try {
            return mineWithSelenium(contributorsReg,contributorsAlt);
        } catch (NumberFormatException ex){
            if (ex.getMessage().split(": ")[1].equals("\"∞\"")){
                //Record error state -2 if repo has "infinite" contributors
                return -2;
            }
            return -1;
        } catch (TimeoutException ex) {
            logger.error("Number of contributors could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }
    }

    private long mineBranchesSelenium(){
        try {
            return mineWithSelenium(branchesReg,branchesAlt);
        } catch (TimeoutException ex) {
            logger.error("Number of branches could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }
    }

    private long mineReleasesSelenium(){
        try {
            return mineWithSelenium(releasesReg,releasesAlt);
        } catch (TimeoutException ex) {
            logger.error("Number of releases could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }

    }

    private long mineWithSelenium(String elementReg, String elementAlt){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementReg)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(elementReg).getText()));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementAlt)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(elementAlt).getText()));
        }
    }

    private long mineWatchersSelenium(){
        try {
            return mineWithSeleniumAlt(watchersSeleniumReg,watchersSeleniumAlt);
        } catch (TimeoutException ex) {
            logger.error("Number of watchers could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }

    }

    private long mineWithSeleniumAlt(String elementReg, String elementAlt){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementReg)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementReg)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(elementReg).getAttribute("aria-label").split(" ")[0]));
        } catch (NoClassDefFoundError ex){
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementAlt)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementAlt)));
            return Long.parseLong(normalizeNumberString(driver.findElementByCssSelector(elementAlt).getAttribute("aria-label").split(" ")[0]));
        }
    }

    private String normalizeNumberString(String input){ return input.trim().replaceAll(",",""); }
}
