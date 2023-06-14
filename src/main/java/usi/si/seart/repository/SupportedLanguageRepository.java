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

    @Query("""
    select sl
    from SupportedLanguage sl
    left join CrawlJob cj
    on cj.language.id = sl.id
    where sl.name in (:names)
    order by cj.crawled nulls first
    """)
    List<SupportedLanguage> findAllByNameInOrderByCrawled(@Param("names") Collection<String> names);

    Boolean existsByName(String name);
}
