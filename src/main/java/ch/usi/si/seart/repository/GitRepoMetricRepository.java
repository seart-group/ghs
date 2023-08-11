package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.join.GitRepoMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;

public interface GitRepoMetricRepository extends JpaRepository<GitRepoMetric, GitRepoMetric.Key> {

    @Modifying
    @Query("delete from GitRepoMetric where repo.id = :id")
    void deleteByRepoId(@NotNull @Param("id") Long repoId);
}
