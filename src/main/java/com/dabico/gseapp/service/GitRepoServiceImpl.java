package com.dabico.gseapp.service;

import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.GitRepoLabelRepository;
import com.dabico.gseapp.repository.GitRepoLanguageRepository;
import com.dabico.gseapp.repository.GitRepoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoServiceImpl implements GitRepoService {
    GitRepoRepository gitRepoRepository;
    GitRepoLabelRepository gitRepoLabelRepository;
    GitRepoLanguageRepository gitRepoLanguageRepository;
    GitRepoConverter gitRepoConverter;

    @Override
    public GitRepoDto getById(Long id){
        return gitRepoConverter.fromGitRepoToGitRepoDto(gitRepoRepository.getOne(id));
    }

    @Override
    public void createOrUpdate(GitRepoDto dto){
        GitRepo repo = GitRepo.builder().build();
        if (dto.getId() != null){
            repo = gitRepoRepository.findById(dto.getId()).orElse(null);
        }
        Set<GitRepoLabel> labels = dto.getLabels().stream().map(gitRepoConverter::fromGitRepoLabelDtoToGitRepoLabel).collect(Collectors.toSet());
        Set<GitRepoLanguage> languages = dto.getLanguages().stream().map(gitRepoConverter::fromGitRepoLanguageDtoToGitRepoLanguage).collect(Collectors.toSet());

        repo.setName(dto.getName());
        repo.setIsFork(dto.getIsFork());
        repo.setCommits(dto.getCommits());
        repo.setBranches(dto.getBranches());
        repo.setDefaultBranch(dto.getDefaultBranch());
        repo.setReleases(dto.getReleases());
        repo.setContributors(dto.getContributors());
        repo.setLicense(dto.getLicense());
        repo.setWatchers(dto.getWatchers());
        repo.setStargazers(dto.getStargazers());
        repo.setForks(dto.getForks());
        repo.setSize(dto.getSize());
        repo.setCreatedAt(dto.getCreatedAt());
        repo.setPushedAt(dto.getPushedAt());
        repo.setUpdatedAt(dto.getUpdatedAt());
        repo.setHomepage(dto.getHomepage());
        repo.setMainLanguage(dto.getMainLanguage());
        repo.setOpenIssues(dto.getOpenIssues());
        repo.setTotalIssues(dto.getTotalIssues());
        repo.setOpenPullRequests(dto.getOpenPullRequests());
        repo.setTotalPullRequests(dto.getTotalPullRequests());
        repo.setLastCommit(dto.getLastCommit());
        repo.setLastCommitSHA(dto.getLastCommitSHA());
        repo.setHasWiki(dto.getHasWiki());
        repo.setIsArchived(dto.getIsArchived());
        repo.setLabels(labels);
        repo.setLanguages(languages);
        gitRepoRepository.save(repo);
    }

    @Override
    public void delete(Long id){ gitRepoRepository.deleteById(id); }
}
