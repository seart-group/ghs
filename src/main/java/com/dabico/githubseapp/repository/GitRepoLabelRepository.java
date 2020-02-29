package com.dabico.githubseapp.repository;

import com.dabico.githubseapp.model.GitRepoLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel,Long> {
}
