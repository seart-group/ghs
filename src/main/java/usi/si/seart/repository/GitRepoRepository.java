package usi.si.seart.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.specification.GitRepoSearch;
import usi.si.seart.repository.specification.GitRepoSpecification;
import usi.si.seart.repository.specification.JpaStreamableSpecificationRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoRepository extends
        JpaRepository<GitRepo, Long>,
        JpaSpecificationExecutor<GitRepo>,
        JpaStreamableSpecificationRepository<GitRepo>
{

    Optional<GitRepo> findGitRepoById(Long id);

    Optional<GitRepo> findGitRepoByNameIgnoreCase(String name);

    @Query(
            "select r.id from GitRepo r " +
            "where r.cloned is null or r.cloned < r.lastCommit " +
            "order by r.cloned asc"
    )
    Stream<Long> findAllRepoWithOutdatedCodeMetrics();

    @Query(
            "select COUNT(r) from GitRepo r " +
            "where r.cloned is null or r.cloned < r.lastCommit"
    )
    Long countAllRepoWithOutdatedCodeMetrics();

    default Page<GitRepo> findAllDynamically(GitRepoSearch parameters, Pageable pageable) {
        GitRepoSpecification specification = new GitRepoSpecification(parameters);
        return findAll(specification, pageable);
    }

    default Stream<GitRepo> streamAllDynamically(GitRepoSearch parameters) {
        GitRepoSpecification specification = new GitRepoSpecification(parameters);
        return stream(specification, GitRepo.class);
    }
}
