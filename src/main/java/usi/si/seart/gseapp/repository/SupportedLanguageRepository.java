package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.List;
import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage,Long> {
    Optional<SupportedLanguage> findByName(String name);
    @Query("SELECT l.name FROM SupportedLanguage l ORDER BY l.name")
    List<String> findAllLanguages();
}
