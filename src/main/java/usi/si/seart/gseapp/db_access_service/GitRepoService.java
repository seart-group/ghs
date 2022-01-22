package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.dto.GitRepoDtoList;
import usi.si.seart.gseapp.dto.GitRepoDtoListPaginated;
import usi.si.seart.gseapp.dto.GitRepoLabelDto;
import usi.si.seart.gseapp.dto.GitRepoLanguageDto;
import usi.si.seart.gseapp.dto.StringList;
import usi.si.seart.gseapp.dto.StringLongDtoList;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;

import java.util.Date;
import java.util.List;

public interface GitRepoService {
    GitRepoDto getRepoById(Long id);
    List<GitRepoLabelDto> findRepoLabels(Long repoId);
    List<GitRepoLanguageDto> findRepoLanguages(Long repoId);

    GitRepoDtoList advancedSearch(String name, Boolean nameEquals, String language, String license, String label, Long commitsMin,
                                  Long commitsMax, Long contributorsMin, Long contributorsMax, Long issuesMin, Long issuesMax,
                                  Long pullsMin, Long pullsMax, Long branchesMin, Long branchesMax, Long releasesMin,
                                  Long releasesMax, Long starsMin, Long starsMax, Long watchersMin, Long watchersMax, Long forksMin,
                                  Long forksMax, Date createdMin, Date createdMax, Date committedMin, Date committedMax,
                                  Boolean excludeForks, Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                  Boolean hasLicense);

    /**
     * @param totalResults If provided, we don't recount total number of result (For the sake of performance)
     */
    GitRepoDtoListPaginated advancedSearch_paginated(String name, Boolean nameEquals, String language, String license, String label,
                                                     Long commitsMin, Long commitsMax, Long contributorsMin, Long contributorsMax,
                                                     Long issuesMin, Long issuesMax, Long pullsMin, Long pullsMax, Long branchesMin,
                                                     Long branchesMax, Long releasesMin, Long releasesMax, Long starsMin,
                                                     Long starsMax, Long watchersMin, Long watchersMax, Long forksMin,
                                                     Long forksMax, Date createdMin, Date createdMax, Date committedMin,
                                                     Date committedMax, Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
                                                     Boolean hasPulls, Boolean hasWiki, Boolean hasLicense, Integer page,
                                                     Integer pageSize, Long totalResults);

    GitRepo createOrUpdateRepo(GitRepo repo);
    StringList getAllLabels();
    StringList getAllLanguages();
    StringList getAllLicenses();
    StringList getAllRepoNames();
    StringLongDtoList getAllLanguageStatistics();
    StringLongDtoList getMainLanguageStatistics();
    void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels);
    void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages);
}
