package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Optional<Topic> findByName(@NotNull String name);

    @Query(
            value = """
            select topic
            from Topic topic
            inner join topic.statistics
            where topic.name like concat('%', :seq, '%')
            order by
                case
                    when topic.name = :seq then 0
                    when topic.name like concat(:seq, '%') then 1
                    when topic.name like concat('%', :seq) then 3
                    else 2
                end,
                topic.statistics.count desc,
                topic.name
            """,
            countQuery = """
            select count(topic)
            from Topic topic
            where topic.name like concat('%', :seq, '%')
            """
    )
    Page<Topic> findAllByNameContainsOrderByBestMatch(@Param("seq") String name, Pageable pageable);
}
