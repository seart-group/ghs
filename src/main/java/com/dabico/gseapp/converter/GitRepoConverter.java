package com.dabico.gseapp.converter;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.GitRepoLabelDto;
import com.dabico.gseapp.dto.GitRepoLanguageDto;
import com.dabico.gseapp.github.GitHubPageCrawlerService;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jsoup.HttpStatusException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.dabico.gseapp.util.DateUtils.fromGitDateString;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GitRepoConverter {
    static final Logger logger = LoggerFactory.getLogger(GitRepoConverter.class);

    ChromeDriver driver;

    @Autowired
    public GitRepoConverter(){
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("chromedriver");
        String driverFile = Objects.requireNonNull(url).getFile();
        System.setProperty("webdriver.chrome.driver", driverFile);
        System.setProperty("webdriver.chrome.silentOutput", "true");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(driverFile))
                .build();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-dev-shm-usage");
        capabilities.setCapability(ChromeOptions.CAPABILITY,options);
        this.driver = new ChromeDriver(service,options);
    }

    public void quitDriver(){
        this.driver.close();
        this.driver.quit();
    }

    public GitRepo jsonToGitRepo(JsonObject json,String language) throws IOException,InterruptedException {
        String repositoryURL = json.get("html_url").getAsString();
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        GitHubPageCrawlerService crawlerService = new GitHubPageCrawlerService(repositoryURL,driver,webDriverWait);
        try {
            crawlerService.mine();
        } catch (HttpStatusException ex) {
            int code = ex.getStatusCode();
            logger.error(ex.getMessage()+": "+ex.getUrl());
            logger.error("Status: "+code);
            if (code == 404){
                logger.error("This repository no longer exists");
                return null;
            } else if (code == 429){
                logger.error("Retrying");
                Thread.sleep(300000);
                return jsonToGitRepo(json,language);
            }
        }
        JsonElement license = json.get("license");
        JsonElement homepage = json.get("homepage");
        return GitRepo.builder()
                      .name(json.get("full_name").getAsString())
                      .isFork(json.get("fork").getAsBoolean())
                      .commits(crawlerService.getCommits())
                      .branches(crawlerService.getBranches())
                      .defaultBranch(json.get("default_branch").getAsString())
                      .releases(crawlerService.getReleases())
                      .contributors(crawlerService.getContributors())
                      .license((license.isJsonNull()) ? null : license.getAsJsonObject()
                                                                      .get("name")
                                                                      .getAsString()
                                                                      .replaceAll("\"",""))
                      .watchers(crawlerService.getWatchers())
                      .stargazers(json.get("stargazers_count").getAsLong())
                      .forks(json.get("forks_count").getAsLong())
                      .size(json.get("size").getAsLong())
                      .createdAt(fromGitDateString(json.get("created_at").getAsString()))
                      .pushedAt(fromGitDateString(json.get("pushed_at").getAsString()))
                      .updatedAt(fromGitDateString(json.get("updated_at").getAsString()))
                      .homepage(homepage.isJsonNull() ? null : homepage.getAsString())
                      .mainLanguage(language)
                      .totalIssues(crawlerService.getTotalIssues())
                      .openIssues(crawlerService.getOpenIssues())
                      .totalPullRequests(crawlerService.getTotalPullRequests())
                      .openPullRequests(crawlerService.getOpenPullRequests())
                      .lastCommit(crawlerService.getLastCommit())
                      .lastCommitSHA(crawlerService.getLastCommitSHA())
                      .hasWiki(json.get("has_wiki").getAsBoolean())
                      .isArchived(json.get("archived").getAsBoolean())
                      .build();
    }

    public List<GitRepoDto> repoListToRepoDtoList(List<GitRepo> repos){
        return repos.stream().map(this::repoToRepoDto).collect(Collectors.toList());
    }

    public List<String[]> repoListToCSVRowList(List<GitRepo> repos){
        List<String[]> results = repos.stream().map(this::repoToCSVRow).collect(Collectors.toList());
        String[] header = new String[]{"name","fork project","commits","branches","default branch","releases",
                                       "contributors","license","watchers","stargazers","forks","size","created",
                                       "pushed","updated","homepage","main language","total issues","open issues",
                                       "total pull requests","open pull requests","last commit","last commit SHA",
                                       "has wiki","is archived"};
        results.add(0, header);
        return results;
    }

    public List<GitRepoLabelDto> labelListToLabelDtoList(List<GitRepoLabel> labels){
        return labels.stream().map(this::labelToLabelDto).collect(Collectors.toList());
    }

    public List<GitRepoLanguageDto> languageListToLanguageDtoList(List<GitRepoLanguage> languages){
        return languages.stream().map(this::languageToLanguageDto).collect(Collectors.toList());
    }

    public GitRepoDto repoToRepoDto(GitRepo repo){
        return GitRepoDto.builder()
                .id(repo.getId())
                .name(repo.getName())
                .isFork(repo.getIsFork())
                .commits(repo.getCommits())
                .branches(repo.getBranches())
                .defaultBranch(repo.getDefaultBranch())
                .releases(repo.getReleases())
                .contributors(repo.getContributors())
                .license(repo.getLicense())
                .watchers(repo.getWatchers())
                .stargazers(repo.getStargazers())
                .forks(repo.getForks())
                .size(repo.getSize())
                .createdAt(repo.getCreatedAt())
                .pushedAt(repo.getPushedAt())
                .updatedAt(repo.getUpdatedAt())
                .homepage(repo.getHomepage())
                .mainLanguage(repo.getMainLanguage())
                .totalIssues(repo.getTotalIssues())
                .openIssues(repo.getOpenIssues())
                .totalPullRequests(repo.getTotalPullRequests())
                .openPullRequests(repo.getOpenPullRequests())
                .lastCommit(repo.getLastCommit())
                .lastCommitSHA(repo.getLastCommitSHA())
                .hasWiki(repo.getHasWiki())
                .isArchived(repo.getIsArchived())
                .build();
    }

    public String[] repoToCSVRow(GitRepo repo){
        String[] attributes = new String[25];
        attributes[0]  = repo.getName();
        attributes[1]  = repo.getIsFork().toString();
        attributes[2]  = repo.getCommits().toString();
        attributes[3]  = repo.getBranches().toString();
        attributes[4]  = repo.getDefaultBranch();
        attributes[5]  = repo.getReleases().toString();
        attributes[6]  = repo.getContributors().toString();
        attributes[7]  = repo.getLicense();
        attributes[8]  = repo.getWatchers().toString();
        attributes[9]  = repo.getStargazers().toString();
        attributes[10] = repo.getForks().toString();
        attributes[11] = repo.getSize().toString();
        attributes[12] = repo.getCreatedAt().toString();
        attributes[13] = repo.getPushedAt().toString();
        attributes[14] = repo.getUpdatedAt().toString();
        attributes[15] = repo.getHomepage();
        attributes[16] = repo.getMainLanguage();
        attributes[17] = repo.getTotalIssues().toString();
        attributes[18] = repo.getOpenIssues().toString();
        attributes[19] = repo.getTotalPullRequests().toString();
        attributes[20] = repo.getOpenPullRequests().toString();
        attributes[21] = repo.getLastCommit().toString();
        attributes[22] = repo.getLastCommitSHA();
        attributes[23] = repo.getHasWiki().toString();
        attributes[24] = repo.getIsArchived().toString();
        return attributes;
    }

    public GitRepoLabelDto labelToLabelDto(GitRepoLabel label){
        return GitRepoLabelDto.builder()
                              .id(label.getId())
                              .label(label.getLabel())
                              .build();
    }

    public GitRepoLanguageDto languageToLanguageDto(GitRepoLanguage language){
        return GitRepoLanguageDto.builder()
                                 .id(language.getId())
                                 .language(language.getLanguage())
                                 .sizeOfCode(language.getSizeOfCode())
                                 .build();
    }
}
