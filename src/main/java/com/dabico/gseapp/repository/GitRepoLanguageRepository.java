package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepoLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage,Long> {
    @Query("select l from GitRepo r left join GitRepoLanguage l where r.id = l.repo.id and l.repo.id = (:id)")
    Set<GitRepoLanguage> findRepoLanguages(@Param("id") Long id);
}
