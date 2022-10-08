package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.SupportedLanguage;

import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {
    Optional<SupportedLanguage> findByName(String name);
}
