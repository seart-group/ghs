package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {
    Optional<GitRepo> findGitRepoByName(String name);
    @Query("select distinct r.mainLanguage,count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    List<Object[]> getLanguageStatistics();
}
