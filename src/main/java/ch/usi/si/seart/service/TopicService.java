package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.repository.TopicRepository;
import ch.usi.si.seart.repository.TopicStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

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
        public Page<Topic> getAll(Pageable pageable) {
            return topicStatisticsRepository.findAll(
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            Sort.Direction.DESC,
                            Topic.Statistics_.COUNT
                    )
            ).map(Topic.Statistics::getTopic);
        }

        @Override
        public Page<Topic> getByNameContains(String name, Pageable pageable) {
            return topicRepository.findAllByNameContainsOrderByBestMatch(
                    name, PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            );
        }
    }
}
