package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.GitRepoDto;

public interface GitRepoService {
    GitRepoDto getById(Long id);
    void createOrUpdate(GitRepoDto dto);
    void delete(Long id);
}
