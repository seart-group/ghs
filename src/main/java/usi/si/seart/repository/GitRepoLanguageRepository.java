package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import usi.si.seart.model.join.GitRepoLanguage;

import javax.validation.constraints.NotNull;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage, GitRepoLanguage.Key> {

    @Modifying
    @Query("delete from GitRepoLanguage where repo.id = :id")
    void deleteByRepoId(@NotNull @Param("id") Long repoId);
}
