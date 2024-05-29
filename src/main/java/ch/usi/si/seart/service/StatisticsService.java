package ch.usi.si.seart.service;

import ch.usi.si.seart.repository.LanguageStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface StatisticsService {

    /**
     * Retrieve the number of mined and analyzed
     * GitHub repositories for each supported language.
     *
     * @return A map where the keys are language names Strings,
     *         that map to the number of corresponding GitHub repositories.
     *         The map entries are sorted in descending fashion by value.
     */
    Map<String, Map<String, Long>> getMainLanguageStats();

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StatisticsServiceImpl implements StatisticsService {

        LanguageStatisticsRepository languageStatisticsRepository;

        @Override
        public Map<String, Map<String, Long>> getMainLanguageStats() {
            return languageStatisticsRepository.findByOrderByMinedDesc().stream()
                    .map(statistics -> Map.entry(
                            statistics.getLanguage().getName(),
                            Map.of(
                                    "mined", statistics.getMined(),
                                    "analyzed", statistics.getAnalyzed()
                            )
                    ))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (key1, key2) -> key2,
                            LinkedHashMap::new
                    ));
        }
    }
}
