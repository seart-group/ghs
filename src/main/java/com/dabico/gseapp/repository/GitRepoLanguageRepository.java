package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepoLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage,Long> {
    @Query("select l from GitRepoLanguage l left join GitRepo r on l.repo.id = r.id where l.repo.id = (:id)")
    List<GitRepoLanguage> findRepoLanguages(@Param("id") Long id);
}
