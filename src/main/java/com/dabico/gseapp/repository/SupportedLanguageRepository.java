package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.SupportedLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportedLanguageRepository extends JpaRepository<SupportedLanguage,Long> {
    Optional<SupportedLanguage> findByName(String name);
}
