package com.dabico.gseapp.service;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.GitRepoLabelDtoList;
import com.dabico.gseapp.dto.GitRepoLanguageDtoList;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.GitRepoLabelRepository;
import com.dabico.gseapp.repository.GitRepoLanguageRepository;
import com.dabico.gseapp.repository.GitRepoRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoServiceImpl implements GitRepoService {
    GitRepoRepository gitRepoRepository;
    GitRepoLabelRepository gitRepoLabelRepository;
    GitRepoLanguageRepository gitRepoLanguageRepository;
    GitRepoConverter gitRepoConverter;

    @Override
    public GitRepoDto getRepoById(Long id){
        return gitRepoConverter.repoToRepoDto(gitRepoRepository.getOne(id));
    }

    public GitRepoLabelDtoList getRepoLabels(Long repoId){
        List<GitRepoLabel> labels = gitRepoLabelRepository.findRepoLabels(repoId);
        return GitRepoLabelDtoList.builder().items(gitRepoConverter.labelListToLabelDtoList(labels)).build();
    }

    public GitRepoLanguageDtoList getRepoLanguages(Long repoId){
        List<GitRepoLanguage> languages = gitRepoLanguageRepository.findRepoLanguages(repoId);
        return GitRepoLanguageDtoList.builder().items(gitRepoConverter.languageListToLanguageDtoList(languages)).build();
    }

    @Override
    public GitRepo createOrUpdateRepo(GitRepo repo){
        Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repo.getName());
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

    public void createOrUpdateLabel(GitRepoLabel label){
        List<GitRepoLabel> existing = gitRepoLabelRepository.findRepoLabels(label.getRepo().getId());
        int index = existing.indexOf(label);
        if (index < 0){
            gitRepoLabelRepository.save(label);
        } else {
            gitRepoLabelRepository.save(existing.get(index));
        }
    }

    public void createOrUpdateLanguage(GitRepoLanguage language){
        List<GitRepoLanguage> existing = gitRepoLanguageRepository.findRepoLanguages(language.getRepo().getId());
        int index = existing.indexOf(language);
        if (index < 0){
            gitRepoLanguageRepository.save(language);
        } else {
            gitRepoLanguageRepository.save(existing.get(index));
        }
    }

    @Override
    public void delete(Long id){ gitRepoRepository.deleteById(id); }
}
