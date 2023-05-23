package usi.si.seart.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query(value = "SELECT t FROM Topic t " +
            "INNER JOIN GitRepoTopic grt ON t.id = grt.topic.id " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(*) DESC")
    @Cacheable(value = "topics")
    List<Topic> findAllSortByPopularity(Pageable pageable);

    Optional<Topic> findByLabel(String label);
}
