package usi.si.seart.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.GitRepo;
import usi.si.seart.projection.GitRepoView;
import usi.si.seart.repository.specification.GitRepoSpecification;
import usi.si.seart.repository.specification.JpaStreamableSpecificationRepository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoRepository extends
        JpaRepository<GitRepo, Long>,
        JpaSpecificationExecutor<GitRepo>,
        JpaStreamableSpecificationRepository<GitRepo>
{
    Optional<GitRepo> findGitRepoById(Long id);

    Optional<GitRepo> findGitRepoByName(String name);

    Optional<GitRepoView> findFirstByIdGreaterThanOrderByIdAsc(Long id);

    @Query("select distinct r.mainLanguage, count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    @Cacheable(value = "languageStatistics")
    List<Tuple> getLanguageStatistics();

    @Query("select distinct r.license from GitRepo r where r.license is not null group by r.license order by count(r.license) desc")
    @Cacheable(value = "licenses")
    List<String> findAllLicenses();

    // Code metrics are outdated if the repository has new commits since the last cloned date or if there are no metrics at all
    @Query("SELECT r FROM GitRepo r WHERE r.cloned is null OR r.cloned < r.lastCommit ORDER BY r.cloned ASC")
    Stream<GitRepo> findAllRepoWithOutdatedCodeMetrics();

    @Query("SELECT COUNT(r) FROM GitRepo r WHERE r.cloned is null OR r.cloned < r.lastCommit")
    Long countAllRepoWithOutdatedCodeMetrics();

    default Page<GitRepo> findAllDynamically(Map<String, ?> parameters, Pageable pageable) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return findAll(specification, pageable);
    }

    default Stream<GitRepo> streamAllDynamically(Map<String, ?> parameters) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return stream(specification, GitRepo.class);
    }
}
