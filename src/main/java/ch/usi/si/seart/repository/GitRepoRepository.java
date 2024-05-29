package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.repository.support.JpaStreamExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.Tuple;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoRepository extends
        JpaRepository<GitRepo, Long>,
        JpaSpecificationExecutor<GitRepo>,
        JpaStreamExecutor<GitRepo>
{

    @Modifying
    @Query("delete from GitRepo where id = :id")
    void deleteById(@NotNull @Param("id") Long id);

    @Modifying
    @Query("update GitRepo set lastPinged = CURRENT_TIMESTAMP() where id = :id")
    void updateLastPingedById(@NotNull @Param("id") Long id);

    Optional<GitRepo> findGitRepoById(Long id);

    Optional<GitRepo> findGitRepoByNameIgnoreCase(String name);

    @Query(
        """
        select
            gitRepo.id as id,
            gitRepo.name as name
        from GitRepo gitRepo
        where DATEDIFF(CURRENT_TIMESTAMP(), gitRepo.lastPinged) > 35
        order by gitRepo.lastPinged, RAND()
        """
    ) // FIXME: 04.08.23 DATEDIFF is vendor-specific
    Stream<Tuple> streamIdentifiersWithOutdatedLastPinged();

    @Query(
        """
        select
            gitRepo.id as id,
            gitRepo.name as name
        from GitRepo gitRepo
        where
            gitRepo.lastAnalyzed is null
        or
            gitRepo.lastAnalyzed < gitRepo.lastCommit
        order by gitRepo.lastAnalyzed, RAND()
        """
    )
    Stream<Tuple> streamIdentifiersWithOutdatedCodeMetrics();

    @Query(
        """
        select COUNT(gitRepo)
        from GitRepo gitRepo
        where DATEDIFF(CURRENT_TIMESTAMP(), gitRepo.lastPinged) > 35
        """
    ) // FIXME: 04.08.23 DATEDIFF is vendor-specific
    Long countWithOutdatedLastPinged();

    @Query(
        """
        select COUNT(gitRepo)
        from GitRepo gitRepo
        where
            gitRepo.lastAnalyzed is null
        or
            gitRepo.lastAnalyzed < gitRepo.lastCommit
        """
    )
    Long countWithOutdatedCodeMetrics();
}
