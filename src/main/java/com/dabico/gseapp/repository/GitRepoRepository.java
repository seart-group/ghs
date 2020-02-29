package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {
}
