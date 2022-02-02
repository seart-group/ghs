package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLanguage;

import java.util.List;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage,Long> {
    @Query("select distinct l.language from GitRepoLanguage l where l is not null")
    List<String> findAllLanguages();
    @Query("select distinct l.language,sum(l.sizeOfCode) from GitRepoLanguage l group by l.language order by sum(l.sizeOfCode) desc")
    List<Object[]> getLanguageStatistics();
    void deleteAllByRepo(GitRepo repo);
}
