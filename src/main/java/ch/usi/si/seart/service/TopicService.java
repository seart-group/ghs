package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.repository.TopicRepository;
import ch.usi.si.seart.repository.TopicStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface TopicService extends NamedEntityService<Topic> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class TopicServiceImpl implements TopicService {

        TopicRepository topicRepository;
        TopicStatisticsRepository topicStatisticsRepository;

        @Override
        public Topic getOrCreate(@NotNull String name) {
            return topicRepository.findByName(name)
                    .orElseGet(() -> topicRepository.save(
                            Topic.builder()
                                    .name(name)
                                    .build()
                    ));
        }

        @Override
        public Collection<Topic> getAll(Pageable pageable) {
            Pageable adjusted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.Direction.DESC,
                    Topic.Statistics_.COUNT
            );
            return topicStatisticsRepository.findAll(adjusted).stream()
                    .map(Topic.Statistics::getTopic)
                    .toList();
        }

        @Override
        public Collection<Topic> getByNameContains(String name, Pageable pageable) {
            return topicRepository.findAllByNameContainsOrderByBestMatch(
                    name, PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            ).getContent();
        }
    }
}
