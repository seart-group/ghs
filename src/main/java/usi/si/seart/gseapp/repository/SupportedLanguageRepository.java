package usi.si.seart.gseapp.repository;

import usi.si.seart.gseapp.model.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage,Long> {
    Optional<SupportedLanguage> findByName(String name);
}
