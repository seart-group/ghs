package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByName(@NotNull String name);
}
