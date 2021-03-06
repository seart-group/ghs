package usi.si.seart.gseapp.repository;

import usi.si.seart.gseapp.model.CrawlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CrawlJobRepository extends JpaRepository<CrawlJob,Long> {
    @Query("select c from CrawlJob c left join SupportedLanguage s on s.id = c.language.id where s.name = (:value)")
    Optional<CrawlJob> findByLanguage(@Param("value") String value);
}
