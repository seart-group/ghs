package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import usi.si.seart.model.CrawlJob;

import java.util.Optional;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {

    @Query("""
    select cj
    from CrawlJob cj
    left join SupportedLanguage sl
    on sl.id = cj.language.id
    where sl.name = (:value)
    """)
    Optional<CrawlJob> findByLanguage(@Param("value") String value);
}
