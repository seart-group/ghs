package com.dabico.gseapp.github_service;

import com.dabico.gseapp.util.DateUtils;
import com.dabico.gseapp.util.LongUtils;
import com.dabico.gseapp.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

@Service
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RepoHtmlPageParserService {
    static final Logger logger = LoggerFactory.getLogger(RepoHtmlPageParserService.class);

    RepoHtmlPageSeleniumParserService repoHtmlPageSeleniumParserService;

    @Autowired
    public RepoHtmlPageParserService(RepoHtmlPageSeleniumParserService repoHtmlPageSeleniumParserService)
    {
        this.repoHtmlPageSeleniumParserService = repoHtmlPageSeleniumParserService;
    }

    public RepoHtmlPageExtraInfo mine(String repoURL) throws IOException
    {
        logger.info("\tMining data for: " + repoURL);

        RepoHtmlPageExtraInfo extraMinedInfo = new RepoHtmlPageExtraInfo();

        jsoupMine_homePage(repoURL, extraMinedInfo);
        seleniumMine_homePage(repoURL, extraMinedInfo);
        jsoupMine_issuesPage(repoURL, extraMinedInfo);
        jsoupMine_pullsPage(repoURL, extraMinedInfo);
        jsoupMine_commitsPage(repoURL, extraMinedInfo);

       return extraMinedInfo;
    }

    private void seleniumMine_homePage(String repoURL, RepoHtmlPageExtraInfo extraInfo) throws IOException {
        Document document = Jsoup.connect(repoURL).userAgent("Mozilla").followRedirects(false).get();
        if (isEmptyRepo(document)){ return; }
        int docIndex = isSponsored(document)? 2: 1;


        if(extraInfo.getWatchers() == null){
            long watchers = repoHtmlPageSeleniumParserService.mineWatchersSelenium(repoURL, docIndex);
            extraInfo.setWatchers(watchers);
        }

        if(extraInfo.getCommits() == null) {
            long commits = repoHtmlPageSeleniumParserService.mineCommitsSelenium(repoURL);
            extraInfo.setCommits(commits);
        }

        if(extraInfo.getBranches() == null) {
            long branches = repoHtmlPageSeleniumParserService.mineBranchesSelenium(repoURL);
            extraInfo.setBranches(branches);
        }

        if(extraInfo.getReleases() == null) {
            long releases = repoHtmlPageSeleniumParserService.mineReleasesSelenium(repoURL);
            extraInfo.setReleases(releases);
        }

        int contributorElementIndex = getContributorElementIndex(document);
        if (contributorElementIndex < 1){ return; }

        if(extraInfo.getContributors() == null) {
            long contributors = repoHtmlPageSeleniumParserService.mineContributorsSelenium(contributorElementIndex, repoURL);
            extraInfo.setContributors(contributors);
        }
    }

    private void jsoupMine_homePage(String repoURL, RepoHtmlPageExtraInfo extraInfo) throws IOException {
        Document document = Jsoup.connect(repoURL).userAgent("Mozilla").followRedirects(false).get();
        if (isEmptyRepo(document)){ return; }
        int docIndex = isSponsored(document)? 2: 1;

        try {
            String watchersReg = String.format(RepoHtmlTags.watchersTemplateReg, docIndex);
            long watchers = LongUtils.getLongValue(document.selectFirst(watchersReg).attr("aria-label").split(" ")[0]);
            extraInfo.setWatchers(watchers);
        } catch (NullPointerException ignored){
            // Later Selenium take care of it.
        }

        try {
            long commits  = LongUtils.getLongValue(document.selectFirst(RepoHtmlTags.commitsReg).html());
            extraInfo.setCommits(commits);
        } catch (NullPointerException ignored) {
            // Later Selenium take care of it.
        } catch (NumberFormatException ex){
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"∞\"")){
                // Record error state -2 if repo has "infinite" commits
                //extraInfo.setCommits(-2); // Emad: Let's wait for Selenium to try it.
            }
        }


        try {
            long branches = LongUtils.getLongValue(document.selectFirst(RepoHtmlTags.branchesReg).html());
            extraInfo.setBranches(branches);
        } catch (NullPointerException ex){
            // Later Selenium take care of it.
        }



        try {
            long releases = LongUtils.getLongValue(document.selectFirst(RepoHtmlTags.releasesReg).html());
            extraInfo.setReleases(releases);
        } catch (NullPointerException ex){
            // Later Selenium take care of it.
        }


        int contributorElementIndex = getContributorElementIndex(document);
        if (contributorElementIndex < 1){ return; }


        try {
            long contributors = LongUtils.getLongValue(document.selectFirst(String.format(RepoHtmlTags.contribTemplateReg, contributorElementIndex)).html());
            extraInfo.setContributors(contributors);
        } catch (NullPointerException ignored){
            // Later Selenium take care of it.
        } catch (NumberFormatException ex){
            if (StringUtils.removeFromStart(ex.getMessage(),18).equals("\"5000+\"")){
                long contributors = 11 + LongUtils.getLongValue(StringUtils.removeFromStartAndEnd(document.selectFirst(String.format(RepoHtmlTags.linkTemplateReg, contributorElementIndex)).html(),2,13));
                extraInfo.setContributors(contributors);
            }
        }
    }

    private void jsoupMine_issuesPage(String repoURL, RepoHtmlPageExtraInfo extraInfo) throws IOException {
        Document document = Jsoup.connect(repoURL + "/issues").userAgent("Mozilla").followRedirects(false).get();
        try {
            long openIssues  = LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(RepoHtmlTags.openReg).text(),5));
            long totalIssues = openIssues + LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(RepoHtmlTags.closedReg).text(),7));
            extraInfo.setOpenIssues(openIssues);
            extraInfo.setTotalIssues(totalIssues);

        } catch (NullPointerException ignored){
            // logger.debug("Issues not parsed.");
            // Later Selenium take care of it.
        }
    }

    private void jsoupMine_pullsPage(String repoURL, RepoHtmlPageExtraInfo extraInfo) throws IOException {
        Document document = Jsoup.connect(repoURL + "/pulls").userAgent("Mozilla").followRedirects(false).get();

        try {
            long openPullRequests  = LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(RepoHtmlTags.openReg).text(),5));
            long totalPullRequests = openPullRequests + LongUtils.getLongValue(StringUtils.removeFromEnd(document.selectFirst(RepoHtmlTags.closedReg).text(),7));
            extraInfo.setOpenPullRequests(openPullRequests);
            extraInfo.setTotalPullRequests(totalPullRequests);
        } catch (NullPointerException ignored){
            // logger.debug("Pull Requests not parsed.");
            // Later Selenium take care of it.
        }
    }

    private void jsoupMine_commitsPage(String repoURL, RepoHtmlPageExtraInfo extraInfo) throws IOException {
        Document document = Jsoup.connect(repoURL + "/commits").userAgent("Mozilla").followRedirects(false).get();
        if (isEmptyRepo(document)){ return; }

        try {
            Date lastCommitDate = DateUtils.fromGitDateString(document.selectFirst(RepoHtmlTags.commitDateReg).attr("datetime"));
            String lastCommitSHA = document.selectFirst(RepoHtmlTags.commitSHAReg).attr("value");
            extraInfo.setLastCommit(lastCommitDate);
            extraInfo.setLastCommitSHA(lastCommitSHA);
        } catch (NullPointerException ignored) {
            // Later Selenium take care of it.
        }
    }


    private boolean isSponsored(Document document){
        try {
            return document.selectFirst(RepoHtmlTags.actionListReg).childrenSize() > 3;
        } catch (NullPointerException ignored) {
            return document.selectFirst(RepoHtmlTags.actionListAlt).childrenSize() > 3;
        }
    }

    private boolean isEmptyRepo(Document document){
        return document.selectFirst("h3:contains(This repository is empty.)") != null;
    }

    private int getContributorElementIndex(Document document){
        int index = 1;
        Elements sidebar = document.selectFirst(RepoHtmlTags.sidebarReg).children();
        for (Element element : sidebar){
            if (element.children().first().children().first().html().contains("> Contributors <")){
                return index;
            }
            index += 1;
        }
        return 0;
    }
}
