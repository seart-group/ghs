package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepoLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel,Long> {
    @Query("select l from GitRepo r left join GitRepoLabel l where r.id = l.repo.id and l.repo.id = (:id)")
    Set<GitRepoLabel> findRepoLabels(@Param("id") Long id);
}
