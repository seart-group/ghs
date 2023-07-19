package usi.si.seart.repository;

import usi.si.seart.model.Language;

import java.util.List;

public interface LanguageStatisticsRepository extends ReadOnlyRepository<Language.Statistics, Long> {

    List<Language.Statistics> findByOrderByMinedDesc();
}
