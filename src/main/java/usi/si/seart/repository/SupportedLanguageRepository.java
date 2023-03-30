package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import usi.si.seart.model.SupportedLanguage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {

    Optional<SupportedLanguage> findByName(String name);

    @Query(
            "select sl " +
            "from SupportedLanguage as sl " +
            "inner join CrawlJob as cj " +
            "on cj.language.id = sl.id " +
            "where sl.name in (:names) " +
            "order by cj.crawled"
    )
    List<SupportedLanguage> findAllByNameInOrderByCrawled(@Param("names") Collection<String> names);

    @Query(
            "select sl " +
            "from SupportedLanguage as sl " +
            "inner join CrawlJob as cj " +
            "on cj.language.id = sl.id " +
            "order by cj.crawled"
    )
    List<SupportedLanguage> findAllOrderByCrawled();

    Boolean existsByName(String name);
}
