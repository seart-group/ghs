package usi.si.seart.gseapp.converter;

import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.dto.GitRepoDtoList;
import usi.si.seart.gseapp.dto.GitRepoLabelDto;
import usi.si.seart.gseapp.dto.GitRepoLanguageDto;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoConverter {
    static Logger logger = LoggerFactory.getLogger(GitRepoConverter.class);


    public List<GitRepoDto> repoListToRepoDtoList(List<GitRepo> repos){
        return repos.stream().map(this::repoToRepoDto).collect(Collectors.toList());
    }

    public List<String[]> repoDtoListToCSVRowList(GitRepoDtoList repos){
        List<String[]> results = repos.getItems().stream().map(this::repoDtoToCSVRow).collect(Collectors.toList());
        String[] header = new String[]{"name","fork project","commits","branches","default branch","releases",
                                       "contributors","license","watchers","stargazers","forks","size","created",
                                       "pushed","updated","homepage","main language","total issues","open issues",
                                       "total pull requests","open pull requests","last commit","last commit SHA",
                                       "has wiki","is archived","languages","labels"};
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

    public String[] repoDtoToCSVRow(GitRepoDto repoDto){
        Long commits = repoDto.getCommits();
        Long branches = repoDto.getBranches();
        Long releases = repoDto.getReleases();
        Long contributors = repoDto.getContributors();
        Long watchers = repoDto.getWatchers();
        Date lastCommit = repoDto.getLastCommit();
        String lastCommitSHA = repoDto.getLastCommitSHA();
        List<GitRepoLanguageDto> languages = repoDto.getLanguages();
        List<GitRepoLabelDto> labels = repoDto.getLabels();

        String[] attributes = new String[27];
        attributes[0]  = repoDto.getName();
        attributes[1]  = repoDto.getIsFork().toString();
        attributes[2]  = (commits==null)?"?":( (commits.equals(Long.MAX_VALUE))?"∞":commits.toString());
        attributes[3]  = (branches!=null) ? branches.toString() : "?";
        attributes[4]  = repoDto.getDefaultBranch();
        attributes[5]  = (releases!=null) ? releases.toString() : "?";
        attributes[6]  = contributors==null?"?":( (contributors.equals(Long.MAX_VALUE))?"∞":contributors.toString());
        attributes[7]  = repoDto.getLicense();
        attributes[8]  = (watchers!=null) ? watchers.toString() : "?";
        attributes[9]  = repoDto.getStargazers().toString();
        attributes[10] = repoDto.getForks().toString();
        attributes[11] = repoDto.getSize().toString();
        attributes[12] = repoDto.getCreatedAt().toString();
        attributes[13] = repoDto.getPushedAt().toString();
        attributes[14] = repoDto.getUpdatedAt().toString();
        attributes[15] = repoDto.getHomepage();
        attributes[16] = repoDto.getMainLanguage();
        attributes[17] = (repoDto.getTotalIssues()!=null)?repoDto.getTotalIssues().toString():"?";
        attributes[18] = (repoDto.getOpenIssues()!=null)?repoDto.getOpenIssues().toString():"?";
        attributes[19] = (repoDto.getTotalPullRequests()!=null)?repoDto.getTotalPullRequests().toString():"?";
        attributes[20] = (repoDto.getOpenPullRequests()!=null)?repoDto.getOpenPullRequests().toString():"?";
        attributes[21] = (lastCommit != null) ? lastCommit.toString() : "?";
        attributes[22] = (lastCommitSHA != null) ? lastCommitSHA : "?";
        attributes[23] = repoDto.getHasWiki().toString();
        attributes[24] = repoDto.getIsArchived().toString();
        attributes[25] = "";
        for (int i = 0; i < languages.size(); ++i){
            if (i == 0){
                attributes[25] += languages.get(i).getLanguage();
            } else {
                attributes[25] += ","+languages.get(i).getLanguage();
            }
        }
        attributes[26] = "";
        for (int i = 0; i < labels.size(); ++i){
            if (i == 0){
                attributes[26] += labels.get(i).getLabel();
            } else {
                attributes[26] += ","+labels.get(i).getLabel();
            }
        }
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
