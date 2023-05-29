package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.GitRepoLanguage;

import javax.persistence.Tuple;
import java.util.List;

public interface GitRepoLanguageRepository extends JpaRepository<GitRepoLanguage, Long> {

    @Query(
            "select distinct l.language, SUM(l.sizeOfCode) as total " +
            "from GitRepoLanguage l " +
            "group by l.language " +
            "order by total desc"
    )
    List<Tuple> getLanguageStatistics();
}
