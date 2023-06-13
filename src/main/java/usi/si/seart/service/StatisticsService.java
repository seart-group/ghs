package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import usi.si.seart.model.view.LabelView;
import usi.si.seart.model.view.TopicView;
import usi.si.seart.repository.LabelViewRepository;
import usi.si.seart.repository.MainLanguageCountRepository;
import usi.si.seart.repository.TopicViewRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface StatisticsService {

    /**
     * Retrieve the number of processed GitHub
     * repositories for each supported language.
     *
     * @return A map where the keys are language names Strings,
     *         that map to the number of corresponding GitHub repositories.
     *         The map entries are sorted in descending fashion by value.
     */
    Map<String, Long> getMainLanguageCount();

    Collection<String> getTopRankedLabelNames();

    Collection<String> getTopRankedTopicNames();

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StatisticsServiceImpl implements StatisticsService {

        LabelViewRepository labelViewRepository;
        TopicViewRepository topicViewRepository;
        MainLanguageCountRepository mainLanguageCountRepository;

        Pageable pageable;

        @Override
        public Map<String, Long> getMainLanguageCount() {
            return mainLanguageCountRepository.findAll().stream()
                    .map(mainLanguageCount -> Map.entry(mainLanguageCount.getName(), mainLanguageCount.getCount()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (key1, key2) -> key2,
                            LinkedHashMap::new
                    ));
        }

        @Override
        public Collection<String> getTopRankedLabelNames() {
            return labelViewRepository.findAll(pageable).stream()
                    .map(LabelView::getName)
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<String> getTopRankedTopicNames() {
            return topicViewRepository.findAll(pageable).stream()
                    .map(TopicView::getName)
                    .collect(Collectors.toList());
        }
    }
}
