package usi.si.seart.gseapp.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;
import usi.si.seart.gseapp.repository.GitRepoLabelRepository;
import usi.si.seart.gseapp.repository.GitRepoLanguageRepository;
import usi.si.seart.gseapp.repository.GitRepoRepository;

import javax.persistence.Tuple;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoServiceImpl implements GitRepoService {

    GitRepoRepository gitRepoRepository;
    GitRepoLabelRepository gitRepoLabelRepository;
    GitRepoLanguageRepository gitRepoLanguageRepository;

    @Override
    public Optional<GitRepo> getRepoById(Long repoId){
        return gitRepoRepository.findGitRepoById(repoId);
    }

    @Override
    public Optional<GitRepo> getByName(String name) {
        return gitRepoRepository.findGitRepoByName(name);
    }

    @Override
    public List<GitRepo> findDynamically(Map<String, Object> parameters) {
        return gitRepoRepository.findAllDynamically(parameters);
    }

    @Override
    public Page<GitRepo> findDynamically(Map<String, Object> parameters, Pageable pageable) {
        return gitRepoRepository.findAllDynamically(parameters, pageable);
    }

    @Override
    public GitRepo createOrUpdateRepo(GitRepo repo){

        if(repo.getWatchers()==null || repo.getCommits() == null || repo.getBranches() == null ||
                repo.getReleases() == null || repo.getContributors() == null  ||
                repo.getLastCommit() == null || repo.getLastCommitSHA()==null)
        {
            log.error("*** REFUSING to store repo data due to incompleteness: {}", repo.getName());
            return null;
        }

        Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repo.getName().toLowerCase());
        if (opt.isPresent()){
            GitRepo existing = opt.get();
            existing.setIsFork(repo.getIsFork());
            existing.setCommits(repo.getCommits());
            existing.setBranches(repo.getBranches());
            existing.setDefaultBranch(repo.getDefaultBranch());
            existing.setReleases(repo.getReleases());
            existing.setContributors(repo.getContributors());
            existing.setLicense(repo.getLicense());
            existing.setWatchers(repo.getWatchers());
            existing.setStargazers(repo.getStargazers());
            existing.setForks(repo.getForks());
            existing.setSize(repo.getSize());
            existing.setCreatedAt(repo.getCreatedAt());
            existing.setPushedAt(repo.getPushedAt());
            existing.setUpdatedAt(repo.getUpdatedAt());
            existing.setHomepage(repo.getHomepage());
            existing.setMainLanguage(repo.getMainLanguage());
            existing.setOpenIssues(repo.getOpenIssues());
            existing.setTotalIssues(repo.getTotalIssues());
            existing.setOpenPullRequests(repo.getOpenPullRequests());
            existing.setTotalPullRequests(repo.getTotalPullRequests());
            existing.setLastCommit(repo.getLastCommit());
            existing.setLastCommitSHA(repo.getLastCommitSHA());
            existing.setHasWiki(repo.getHasWiki());
            existing.setIsArchived(repo.getIsArchived());
            return gitRepoRepository.save(existing);
        } else {
            return gitRepoRepository.save(repo);
        }
    }

    @Override
    public List<String> getAllLabels(Integer limit){
        return gitRepoLabelRepository.findMostFrequentLabels(PageRequest.of(0, limit));
    }

    @Override
    public List<String> getAllLanguages(){
        return gitRepoLanguageRepository.findAllLanguages();
    }

    @Override
    public List<String> getAllLicenses(){
        return gitRepoRepository.findAllLicenses();
    }

    @Override
    public List<String> getAllRepoNames(){
        return gitRepoRepository.findAllRepoNames();
    }


    /**
     * Retrieve the cumulative size (in bytes) of all source files written in a language,
     * across all processed GitHub repositories.
     *
     * @return A map where the keys are language names Strings,
     *         that map to the Long value representing byte size for each language.
     *         The map entries are sorted in descending fashion by value.
     */
    @Override
    public Map<String, Long> getAllLanguageStatistics(){
        return getLanguageStatistics(gitRepoLanguageRepository::getLanguageStatistics);
    }

    /**
     * Retrieve the number of processed GitHub repositories for each main (supported) language.
     *
     * @return A map where the keys are language names Strings,
     *         that map to Long values of the number of corresponding GitHub repositories.
     *         The map entries are sorted in descending fashion by value.
     */
    @Override
    public Map<String, Long> getMainLanguageStatistics(){
        return getLanguageStatistics(gitRepoRepository::getLanguageStatistics);
    }

    private Map<String, Long> getLanguageStatistics(Supplier<List<Tuple>> tupleListSupplier){
        List<Tuple> languages = tupleListSupplier.get();
        return languages.stream()
                .map(tuple -> Map.entry(tuple.get(0, String.class), tuple.get(1, Long.class)))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    @Override
    @Transactional
    public void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels){
        gitRepoLabelRepository.deleteAll(repo.getLabels());
        gitRepoLabelRepository.saveAll(labels);
    }

    @Override
    @Transactional
    public void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages){
        gitRepoLanguageRepository.deleteAll(repo.getLanguages());
        gitRepoLanguageRepository.saveAll(languages);
    }
}
