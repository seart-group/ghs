package usi.si.seart.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.GitRepo;
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

    @Query("select distinct r.mainLanguage, count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    @Cacheable(value = "languageStatistics")
    List<Tuple> getLanguageStatistics();

    @Query("select distinct r.license from GitRepo r where r.license is not null group by r.license order by count(r.license) desc")
    @Cacheable(value = "licenses")
    List<String> findAllLicenses();

    @Query("select r.name from GitRepo r order by r.crawled asc")
    List<String> findAllRepoNames();

    default List<GitRepo> findAllDynamically(Map<String, ?> parameters) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return findAll(specification);
    }

    default Page<GitRepo> findAllDynamically(Map<String, ?> parameters, Pageable pageable) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return findAll(specification, pageable);
    }

    default Stream<GitRepo> streamAllDynamically(Map<String, ?> parameters) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return stream(specification, GitRepo.class);
    }
}
