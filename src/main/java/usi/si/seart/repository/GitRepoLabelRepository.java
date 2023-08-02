package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import usi.si.seart.model.join.GitRepoLabel;

import javax.validation.constraints.NotNull;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel, GitRepoLabel.Key> {

    @Modifying
    @Query("DELETE FROM GitRepoLabel WHERE repo.id = :id")
    void deleteByRepoId(@NotNull @Param("id") Long repoId);
}
