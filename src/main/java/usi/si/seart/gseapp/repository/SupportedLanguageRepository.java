package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.gseapp.model.SupportedLanguage;

import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage, Long> {
    Optional<SupportedLanguage> findByName(String name);
}
