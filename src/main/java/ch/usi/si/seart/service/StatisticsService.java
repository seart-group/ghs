package ch.usi.si.seart.service;

import ch.usi.si.seart.model.view.TopicView;
import ch.usi.si.seart.repository.LanguageStatisticsRepository;
import ch.usi.si.seart.repository.TopicViewRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

    Collection<String> getTopRankedLabelNames();

    Collection<String> getTopRankedTopicNames();

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StatisticsServiceImpl implements StatisticsService {

        TopicViewRepository topicViewRepository;
        LanguageStatisticsRepository languageStatisticsRepository;

        Pageable pageable;

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

        @Override
        public Collection<String> getTopRankedLabelNames() {
            // TODO: FIX OR REMOVE
            return List.of();
        }

        @Override
        public Collection<String> getTopRankedTopicNames() {
            return topicViewRepository.findAll(pageable).stream()
                    .map(TopicView::getName)
                    .toList();
        }
    }
}
