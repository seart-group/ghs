package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.join.GitRepoLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.validation.constraints.NotNull;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel, GitRepoLabel.Key> {

    @Modifying
    @Query("delete from GitRepoLabel where repo.id = :id")
    void deleteByRepoId(@NotNull @Param("id") Long repoId);
}
