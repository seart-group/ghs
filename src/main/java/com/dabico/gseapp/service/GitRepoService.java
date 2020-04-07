package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.StringList;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;

public interface GitRepoService {
    GitRepoDto getRepoById(Long id);
    GitRepo createOrUpdateRepo(GitRepo repo);
    void createOrUpdateLabel(GitRepoLabel label);
    void createOrUpdateLanguage(GitRepoLanguage language);
    StringList getAllLabels();
    StringList getAllLanguages();
}
