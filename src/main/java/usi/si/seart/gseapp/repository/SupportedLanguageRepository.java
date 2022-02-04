package usi.si.seart.gseapp.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.List;
import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {
    Optional<SupportedLanguage> findByName(String name);

    @Query("select l.name from SupportedLanguage l order by l.name")
    @Cacheable(value = "languages")
    List<String> findAllLanguages();
}
