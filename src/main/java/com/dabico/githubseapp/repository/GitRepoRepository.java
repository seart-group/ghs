package com.dabico.githubseapp.repository;

import com.dabico.githubseapp.model.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {
}
