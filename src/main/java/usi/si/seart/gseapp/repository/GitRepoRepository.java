package usi.si.seart.gseapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.repository.specification.GitRepoSpecification;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long>, JpaSpecificationExecutor<GitRepo> {
    Optional<GitRepo> findGitRepoById(Long id);
    Optional<GitRepo> findGitRepoByName(String name);
    @Query("select distinct r.mainLanguage, count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    List<Tuple> getLanguageStatistics();
    @Query("select distinct r.license from GitRepo r where r.license is not null group by r.license order by count(r.license) desc")
    List<String> findAllLicenses();
    @Query("SELECT r.name FROM GitRepo r ORDER BY r.crawled ASC")
    List<String> findAllRepoNames();

    default List<GitRepo> findAllDynamically(Map<String, ?> parameters) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return findAll(specification);
    }

    default Page<GitRepo> findAllDynamically(Map<String, ?> parameters, Pageable pageable) {
        GitRepoSpecification specification = GitRepoSpecification.from(parameters);
        return findAll(specification, pageable);
    }
}
