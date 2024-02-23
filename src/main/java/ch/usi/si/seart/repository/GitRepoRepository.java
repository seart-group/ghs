package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.Tuple;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoRepository extends
        JpaRepository<GitRepo, Long>,
        JpaSpecificationExecutor<GitRepo>
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
        select r.id as id, r.name as name from GitRepo r
        where DATEDIFF(CURRENT_TIMESTAMP(), r.lastPinged) > 35
        order by r.lastPinged, RAND()
        """
    ) // FIXME: 04.08.23 DATEDIFF is vendor-specific
    Stream<Tuple> streamIdentifiersWithOutdatedLastPinged();

    @Query(
        """
        select r.id as id, r.name as name from GitRepo r
        where r.lastAnalyzed is null or r.lastAnalyzed < r.lastCommit
        order by r.lastAnalyzed, RAND()
        """
    )
    Stream<Tuple> streamIdentifiersWithOutdatedCodeMetrics();

    @Query(
        """
        select COUNT(r) from GitRepo r
        where DATEDIFF(CURRENT_TIMESTAMP(), r.lastPinged) > 35
        """
    ) // FIXME: 04.08.23 DATEDIFF is vendor-specific
    Long countWithOutdatedLastPinged();

    @Query(
        """
        select COUNT(r) from GitRepo r
        where r.lastAnalyzed is null or r.lastAnalyzed < r.lastCommit
        """
    )
    Long countWithOutdatedCodeMetrics();
}
