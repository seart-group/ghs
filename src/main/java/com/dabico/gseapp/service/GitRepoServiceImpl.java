package com.dabico.gseapp.service;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.dto.*;
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

import java.util.HashSet;
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
    public GitRepoDto getRepoById(Long repoId){
        GitRepo repo = gitRepoRepository.getOne(repoId);
        List<GitRepoLabel> labels = gitRepoLabelRepository.findRepoLabels(repoId);
        List<GitRepoLanguage> languages = gitRepoLanguageRepository.findRepoLanguages(repoId);
        GitRepoDto repoDto = gitRepoConverter.repoToRepoDto(repo);
        repoDto.setLabels(new HashSet<>(gitRepoConverter.labelListToLabelDtoList(labels)));
        repoDto.setLanguages(new HashSet<>(gitRepoConverter.languageListToLanguageDtoList(languages)));
        return repoDto;
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

    @Override
    public StringList getAllLabels(){
        return StringList.builder().items(gitRepoLabelRepository.findAllLabels()).build();
    }

    @Override
    public StringList getAllLanguages(){
        return StringList.builder().items(gitRepoLanguageRepository.findAllLanguages()).build();
    }

    @Override
    public void createOrUpdateLabel(GitRepoLabel label){
        List<GitRepoLabel> existing = gitRepoLabelRepository.findRepoLabels(label.getRepo().getId());
        int index = existing.indexOf(label);
        if (index < 0){
            gitRepoLabelRepository.save(label);
        } else {
            gitRepoLabelRepository.save(existing.get(index));
        }
    }

    @Override
    public void createOrUpdateLanguage(GitRepoLanguage language){
        List<GitRepoLanguage> existing = gitRepoLanguageRepository.findRepoLanguages(language.getRepo().getId());
        int index = existing.indexOf(language);
        if (index < 0){
            gitRepoLanguageRepository.save(language);
        } else {
            gitRepoLanguageRepository.save(existing.get(index));
        }
    }
}
