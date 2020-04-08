package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.GitRepoDtoListPaginated;
import com.dabico.gseapp.dto.StringList;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;

import java.util.Date;

public interface GitRepoService {
    GitRepoDto getRepoById(Long id);
    GitRepoDtoListPaginated advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                           Long commitsMin, Long commitsMax, Long contributorsMin, Long contributorsMax,
                                           Long issuesMin, Long issuesMax, Long pullsMin, Long pullsMax, Long branchesMin,
                                           Long branchesMax, Long releasesMin, Long releasesMax, Long starsMin,
                                           Long starsMax, Long watchersMin, Long watchersMax, Long forksMin,
                                           Long forksMax, Date createdMin, Date createdMax, Date committedMin,
                                           Date committedMax, Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
                                           Boolean hasPulls, Boolean hasWiki, Boolean hasLicense, Integer page,
                                           Integer pageSize);
    GitRepo createOrUpdateRepo(GitRepo repo);
    void createOrUpdateLabel(GitRepoLabel label);
    void createOrUpdateLanguage(GitRepoLanguage language);
    StringList getAllLabels();
    StringList getAllLanguages();
}
