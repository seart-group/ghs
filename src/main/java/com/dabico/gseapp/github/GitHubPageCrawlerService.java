package com.dabico.gseapp.github;

import com.dabico.gseapp.util.DateUtils;
import com.dabico.gseapp.util.LongUtils;
import com.dabico.gseapp.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitHubPageCrawlerService {
    static final Logger logger = LoggerFactory.getLogger(GitHubPageCrawlerService.class);

    static String commitsReg          = "#js-repo-pjax-container > div > div > div > div > div > div > div > div:nth-child(4) > ul > li:nth-child(1) > a > span > strong";
    static String commitsAlt          = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.Box.mb-3 > div.Box-header.Box-header--blue.position-relative > div > div:nth-child(4) > ul > li:nth-child(1) > a > span > strong";
    static String branchesReg         = "#js-repo-pjax-container > div > div > div > div > div > div > div > div:nth-child(4) > ul > li:nth-child(2) > a > strong";
    static String branchesAlt         = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.Box.mb-3 > div.Box-header.Box-header--blue.position-relative > div > div:nth-child(4) > ul > li:nth-child(2) > a > strong";
    static String releasesReg         = "#js-repo-pjax-container > div > div > div > div > div > div > div > div:nth-child(4) > ul > li:nth-child(3) > a > strong";
    static String releasesAlt         = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.Box.mb-3 > div.Box-header.Box-header--blue.position-relative > div > div:nth-child(4) > ul > li:nth-child(3) > a > strong";
    static String actionListReg       = "#js-repo-pjax-container > div > div > ul";
    static String actionListAlt       = "#js-repo-pjax-container > div.pagehead.repohead.readability-menu.bg-gray-light.pb-0.pt-3.border-0.mb-5 > div.d-flex.mb-3.px-3.px-md-4.px-lg-5 > ul";
    static String watchersTemplateReg = "#js-repo-pjax-container > div > div > ul > li:nth-child(%d) > a:nth-child(2)";
    static String watchersTemplateAlt = "#js-repo-pjax-container > div.pagehead.repohead.hx_repohead.readability-menu.bg-gray-light.pb-0.pt-3 > div > ul > li:nth-child(%d) > a:nth-child(2)";
    static String sidebarReg          = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div";
    static String contribTemplateReg  = "#js-repo-pjax-container > div > div > div > div > div > div:nth-child(%d) > div > h2 > a > span";
    static String contribTemplateAlt  = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div > div:nth-child(%d) > div > h2 > a > span";
    static String linkTemplateReg     = "#js-repo-pjax-container > div > div > div > div > div > div:nth-child(%d) > div > div > a";
    static String linkTemplateAlt     = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div > div:nth-child(%d) > div > div > a";
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

        if (isEmptyRepo(document)){ return; }

        int index = 1;
        if (isSponsored(document)){ index += 1; }

        try {
            String watchersReg = String.format(watchersTemplateReg,index);
            watchers = LongUtils.getLongValue(document.selectFirst(watchersReg).attr("aria-label").split(" ")[0]);
        } catch (NullPointerException ignored){
            watchers = mineWatchersSelenium(index);
        }

        try {
            commits  = LongUtils.getLongValue(document.selectFirst(commitsReg).html());
        } catch (NullPointerException ignored) {
            commits  = mineCommitsSelenium();
        } catch (NumberFormatException ex){
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"∞\"")){
                //Record error state -2 if repo has "infinite" commits
                commits = -2;
            } else {
                commits = -1;
            }
        }

        try {
            branches = LongUtils.getLongValue(document.selectFirst(branchesReg).html());
        } catch (NullPointerException ex){
            branches = mineBranchesSelenium();
        }

        try {
            releases = LongUtils.getLongValue(document.selectFirst(releasesReg).html());
        } catch (NullPointerException ex){
            releases = mineReleasesSelenium();
        }

        index = getContributorElementIndex(document);
        if (index < 1){ return; }

        try {
            contributors = LongUtils.getLongValue(document.selectFirst(String.format(contribTemplateReg,index)).html());
        } catch (NullPointerException ignored){
            contributors = mineContributorsSelenium(index);
        } catch (NumberFormatException ex){
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"5000+\"")){
                contributors = 11 + LongUtils.getLongValue(StringUtils.removeFromStartAndEnd(document.selectFirst(String.format(linkTemplateReg,index)).html(),2,13));
            } else {
                contributors = -1;
            }
        }
    }

    private void mineIssuesPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/issues").userAgent("Mozilla").followRedirects(false).get();
        try {
            openIssues  = LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(openReg).text(),5));
            totalIssues = openIssues + LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(closedReg).text(),7));
        } catch (NullPointerException ignored){}
    }

    private void minePullsPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/pulls").userAgent("Mozilla").followRedirects(false).get();
        try {
            openPullRequests  = LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(openReg).text(),5));
            totalPullRequests = openPullRequests + LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(closedReg).text(),7));
        } catch (NullPointerException ignored){}
    }

    private void mineCommitsPage() throws IOException {
        Document document = Jsoup.connect(repoURL + "/commits").userAgent("Mozilla").followRedirects(false).get();
        if (isEmptyRepo(document)){ return; }

        try {
            lastCommit = DateUtils.fromGitDateString(document.selectFirst(commitDateReg).attr("datetime"));
            lastCommitSHA = document.selectFirst(commitSHAReg).attr("value");
        } catch (NullPointerException ignored) {
            logger.error("Error locating commits");
        }
    }

    private long mineWatchersSelenium(int index){
        try {
            String watchersReg = String.format(watchersTemplateReg,index);
            String watchersAlt = String.format(watchersTemplateAlt,index);
            return LongUtils.getLongValue(StringUtils.removeFromEnd(getPageElement(watchersReg,watchersAlt).getAttribute("aria-label"),35));
        } catch (TimeoutException ex) {
            logger.error("Number of watchers could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }
    }

    private long mineCommitsSelenium(){
        return mineCommitsSelenium(0);
    }

    private long mineCommitsSelenium(int attempt){
        try {
            return LongUtils.getLongValue(getPageElement(commitsReg,commitsAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of commits!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of commits could not be mined at this time!");
                return -1;
            } else {
                return mineCommitsSelenium(++attempt);
            }
        }
    }

    private long mineBranchesSelenium(){
        return mineBranchesSelenium(0);
    }

    private long mineBranchesSelenium(int attempt){
        try {
            return LongUtils.getLongValue(getPageElement(branchesReg,branchesAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of branches!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of branches could not be mined at this time!");
                return -1;
            } else {
                return mineBranchesSelenium(++attempt);
            }
        }
    }

    private long mineReleasesSelenium(){
        return mineReleasesSelenium(0);
    }

    private long mineReleasesSelenium(int attempt){
        try {
            return LongUtils.getLongValue(getPageElement(releasesReg,releasesAlt).getText());
        } catch (TimeoutException ex) {
            logger.error("Selenium could not locate number of releases!");
            logger.error("Retrying");
            if (attempt > 2){
                logger.error("Number of releases could not be mined at this time!");
                return -1;
            } else {
                return mineReleasesSelenium(++attempt);
            }
        }
    }

    private long mineContributorsSelenium(int index){
        try {
            String contributorsReg = String.format(contribTemplateReg,index);
            String contributorsAlt = String.format(contribTemplateAlt,index);
            return LongUtils.getLongValue(getPageElement(contributorsReg,contributorsAlt).getText());
        } catch (NumberFormatException ex){
            String linkReg = String.format(linkTemplateReg,index);
            String linkAlt = String.format(linkTemplateAlt,index);
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"5000+\"")){
                contributors = 11 + LongUtils.getLongValue(StringUtils.removeFromStartAndEnd(getPageElement(linkReg,linkAlt).getText(),2,13));
            }
            return -1;
        } catch (TimeoutException ex) {
            logger.error("Number of contributors could not be mined at this time!");
            logger.error("Reason: Selenium could not locate the specified element");
            return -1;
        }
    }

    private WebElement getPageElement(String elementReg, String elementAlt){
        driver.get(repoURL);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementReg)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementReg)));
            return driver.findElementByCssSelector(elementReg);
        } catch (TimeoutException ex) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(elementAlt)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(elementAlt)));
            return driver.findElementByCssSelector(elementAlt);
        }
    }

    private boolean isSponsored(Document document){
        try {
            return document.selectFirst(actionListReg).childrenSize() > 3;
        } catch (NullPointerException ignored) {
            return document.selectFirst(actionListAlt).childrenSize() > 3;
        }
    }

    private boolean isEmptyRepo(Document document){
        return document.selectFirst("h3:contains(This repository is empty.)") != null;
    }

    private int getContributorElementIndex(Document document){
        int index = 1;
        Elements sidebar = document.selectFirst(sidebarReg).children();
        for (Element element : sidebar){
            if (element.children().first().children().first().html().contains("> Contributors <")){
                return index;
            }
            index += 1;
        }
        return 0;
    }
}
