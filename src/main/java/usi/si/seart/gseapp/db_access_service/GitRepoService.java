package usi.si.seart.gseapp.db_access_service;

import com.google.common.collect.Range;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GitRepoService {
    Optional<GitRepo> getRepoById(Long id);
    Optional<GitRepo> getByName(String name);

    List<GitRepo> findDynamically(
            String name, Boolean nameEquals, String language, String license, String label, Range<Long> commits,
            Range<Long> contributors, Range<Long> issues, Range<Long> pulls, Range<Long> branches, Range<Long> releases,
            Range<Long> stars, Range<Long> watchers, Range<Long> forks, Range<Date> created, Range<Date> committed,
            Boolean excludeForks, Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki, Boolean hasLicense
    );

    Page<GitRepo> findDynamically(
            String name, Boolean nameEquals, String language, String license, String label, Range<Long> commits,
            Range<Long> contributors, Range<Long> issues, Range<Long> pulls, Range<Long> branches, Range<Long> releases,
            Range<Long> stars, Range<Long> watchers, Range<Long> forks, Range<Date> created, Range<Date> committed,
            Boolean excludeForks, Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki, Boolean hasLicense,
            Pageable pageable
    );

    GitRepo createOrUpdateRepo(GitRepo repo);
    List<String> getAllLabels(Integer limit);
    List<String> getAllLanguages();
    List<String> getAllLicenses();
    List<String> getAllRepoNames();
    Map<String, Long> getAllLanguageStatistics();
    Map<String, Long> getMainLanguageStatistics();
    void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels);
    void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages);
}
