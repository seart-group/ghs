package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.support.ReadOnlyRepository;

import java.util.List;

public interface LanguageStatisticsRepository extends ReadOnlyRepository<Language.Statistics, Long> {

    List<Language.Statistics> findByOrderByMinedDesc();
}
