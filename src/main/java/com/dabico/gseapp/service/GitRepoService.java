package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.model.GitRepo;

public interface GitRepoService {
    GitRepoDto getById(Long id);
    void createOrUpdateRepo(GitRepo repo);
    void delete(Long id);
}
