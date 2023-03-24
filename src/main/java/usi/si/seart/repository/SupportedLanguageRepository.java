package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.SupportedLanguage;

import java.util.List;
import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {

    Optional<SupportedLanguage> findByName(String name);

    @Query(
            "select sl " +
            "from SupportedLanguage as sl " +
            "inner join CrawlJob as cj " +
            "on cj.language.id = sl.id " +
            "order by cj.crawled"
    )
    List<SupportedLanguage> findAllOrderByCrawled();
}
