package usi.si.seart.gseapp.repository;

import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.util.interval.DateInterval;
import usi.si.seart.gseapp.util.interval.LongInterval;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GitRepoRepositoryCustom {
//    Long countResults(String name, Boolean nameEquals, String language, String license, String label,
//                      LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
//                      LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
//                      LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
//                      Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
//                      Boolean hasLicense);

//    List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
//                                 LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
//                                 LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
//                                 LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
//                                 Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
//                                 Boolean hasLicense, Pageable pageable);

    List<GitRepoDto> advancedSearch_emad(String name, Boolean nameEquals, String language, String license, String label,
                                                LongInterval commits, LongInterval contributors, LongInterval issues,
                                                LongInterval pulls, LongInterval branches, LongInterval releases,
                                                LongInterval stars, LongInterval watchers, LongInterval forks,
                                                DateInterval created, DateInterval committed, Boolean excludeForks,
                                                Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                                Boolean hasLicense, Pageable pageable);

    Long countAdvancedSearch_emad(String name, Boolean nameEquals, String language, String license, String label,
                                         LongInterval commits, LongInterval contributors, LongInterval issues,
                                         LongInterval pulls, LongInterval branches, LongInterval releases,
                                         LongInterval stars, LongInterval watchers, LongInterval forks,
                                         DateInterval created, DateInterval committed, Boolean excludeForks,
                                         Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                         Boolean hasLicense);
}
