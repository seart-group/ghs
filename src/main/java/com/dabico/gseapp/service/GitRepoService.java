package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;

public interface GitRepoService {
    GitRepoDto getRepoById(Long id);
    void createOrUpdateRepo(GitRepo repo);
    void createOrUpdateRepoLabel(GitRepoLabel label);
    void createOrUpdateRepoLanguage(GitRepoLanguage language);
    void delete(Long id);
}
