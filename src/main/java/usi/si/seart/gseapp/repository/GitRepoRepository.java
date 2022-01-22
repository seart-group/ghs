package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.GitRepo;

import java.util.List;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {
    Optional<GitRepo> findGitRepoByName(String name);
    @Query("select distinct r.mainLanguage,count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    List<Object[]> getLanguageStatistics();
    @Query("select distinct r.license from GitRepo r where r.license is not null group by r.license order by count(r.license) desc")
    List<String> findAllLicenses();
    @Query("SELECT r.name FROM GitRepo r ORDER BY r.crawled ASC")
    List<String> findAllRepoNames();
}
