package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {
    Optional<GitRepo> findGitRepoByName(String name);
}
