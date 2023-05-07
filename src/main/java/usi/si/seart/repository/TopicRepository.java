package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query(value = "SELECT t.* FROM topics t " +
            "INNER JOIN repo_topics grt ON t.id = grt.topic_id " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<Topic> findAllSortByPopularity();
    Optional<Topic> findByLabel(String label);
}