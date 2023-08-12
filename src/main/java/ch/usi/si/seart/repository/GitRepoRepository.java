package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.repository.specification.GitRepoSearch;
import ch.usi.si.seart.repository.specification.GitRepoSpecification;
import ch.usi.si.seart.repository.specification.JpaStreamableSpecificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        JpaStreamableSpecificationRepository<GitRepo>
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
            r.id as id,
            r.name as name
        from GitRepo r
        where (
            r.commits is null or
            r.branches is null or
            r.releases is null or
            r.contributors is null or
            r.totalIssues is null or
            r.openIssues is null or
            r.totalPullRequests is null or
            r.openPullRequests is null or
            r.lastCommit is null or
            r.lastCommitSHA is null
        )
        """
    )
    Stream<Tuple> streamIdentifiersHavingMissingData();

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
        select COUNT(r)
        from GitRepo r
        where (
            r.commits is null or
            r.branches is null or
            r.releases is null or
            r.contributors is null or
            r.totalIssues is null or
            r.openIssues is null or
            r.totalPullRequests is null or
            r.openPullRequests is null or
            r.lastCommit is null or
            r.lastCommitSHA is null
        )
        """
    )
    Long countHavingMissingData();

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

    default Page<GitRepo> findAllDynamically(GitRepoSearch parameters, Pageable pageable) {
        GitRepoSpecification specification = new GitRepoSpecification(parameters);
        return findAll(specification, pageable);
    }

    default Stream<GitRepo> streamAllDynamically(GitRepoSearch parameters) {
        GitRepoSpecification specification = new GitRepoSpecification(parameters);
        return stream(specification, GitRepo.class);
    }
}
