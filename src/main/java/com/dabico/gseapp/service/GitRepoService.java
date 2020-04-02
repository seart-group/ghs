package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;

import java.util.List;

public interface GitRepoService {
    GitRepoDto getRepoById(Long id);
    GitRepo createOrUpdateRepo(GitRepo repo);
    void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels);
    void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages);
}
