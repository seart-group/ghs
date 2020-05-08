package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel,Long> {
    @Query("select l from GitRepoLabel l left join GitRepo r on l.repo.id = r.id where l.repo.id = (:id)")
    List<GitRepoLabel> findRepoLabels(@Param("id") Long id);
    @Query("select distinct l.label from GitRepoLabel l where l is not null group by l.label order by count(l.label) desc")
    List<String> findAllLabels();
    void deleteAllByRepo(GitRepo repo);
}
