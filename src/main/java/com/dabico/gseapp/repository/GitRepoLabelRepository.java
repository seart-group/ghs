package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepoLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel,Long> {
}
