package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.model.view.TopicView;
import ch.usi.si.seart.repository.TopicRepository;
import ch.usi.si.seart.repository.TopicViewRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface TopicService extends NamedEntityService<Topic> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class TopicServiceImpl implements TopicService {

        TopicRepository topicRepository;
        TopicViewRepository topicViewRepository;

        Pageable pageable;

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
        public Collection<Topic> getRanked() {
            Collection<String> names = topicViewRepository.findAll(pageable).stream()
                    .map(TopicView::getName)
                    .toList();
            return topicRepository.findAllByNameIn(names);
        }
    }
}
