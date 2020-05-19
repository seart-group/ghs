package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.util.interval.DateInterval;
import com.dabico.gseapp.util.interval.LongInterval;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GitRepoRepositoryCustom {
    Long countResults(String name, Boolean nameEquals, String language, String license, String label,
                      LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
                      LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
                      LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
                      Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                      Boolean hasLicense);
    List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                 LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
                                 LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
                                 LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
                                 Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                 Boolean hasLicense);
    List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                 LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
                                 LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
                                 LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
                                 Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                 Boolean hasLicense, Pageable pageable);
}