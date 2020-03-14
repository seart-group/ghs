package com.dabico.gseapp.service;

import com.dabico.gseapp.repository.GitRepoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GitRepoServiceImpl implements GitRepoService {
    GitRepoRepository gitRepoRepository;
}
