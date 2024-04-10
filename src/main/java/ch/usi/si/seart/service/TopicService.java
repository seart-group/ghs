package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.repository.TopicRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

public interface TopicService extends NamedEntityService<Topic> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class TopicServiceImpl implements TopicService {

        TopicRepository topicRepository;

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
        public Collection<Topic> getAll() {
            // TODO: FIX OR REMOVE
            return List.of();
        }
    }
}
