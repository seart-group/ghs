package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepoLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage,Long> {
    @Query("select l from GitRepoLanguage l left join GitRepo r on l.repo.id = r.id where l.repo.id = (:id)")
    List<GitRepoLanguage> findRepoLanguages(@Param("id") Long id);
    @Query("select distinct l.language from GitRepoLanguage l where l is not null")
    List<String> findAllLanguages();
    @Query("select distinct l.language,sum(l.sizeOfCode) from GitRepoLanguage l group by l.language order by sum(l.sizeOfCode) desc")
    List<Object[]> getLanguageStatistics();
}
