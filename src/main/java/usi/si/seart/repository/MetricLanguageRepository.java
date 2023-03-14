package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.MetricLanguage;

import java.util.Optional;

public interface MetricLanguageRepository extends JpaRepository<MetricLanguage, Long> {

    Optional<Long> findLanguageIdByLanguage(String language);
}