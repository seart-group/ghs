package usi.si.seart.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoLabel;
import usi.si.seart.model.GitRepoLanguage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoService {
    Optional<GitRepo> getRepoById(Long id);
    Optional<GitRepo> getByName(String name);
    Page<GitRepo> findDynamically(Map<String, Object> parameters, Pageable pageable);
    Stream<GitRepo> streamDynamically(Map<String, Object> parameters);
    GitRepo createOrUpdateRepo(GitRepo repo);
    List<String> getAllLabels(Integer limit);
    List<String> getAllLanguages();
    List<String> getAllLicenses();
    Map<String, Long> getAllLanguageStatistics();
    Map<String, Long> getMainLanguageStatistics();
    void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels);
    void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages);
}
