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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface TopicService extends NamedEntityService<Topic> {

    Collection<Topic> getByNameContains(String name, Pageable pageable);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class TopicServiceImpl implements TopicService {

        TopicRepository topicRepository;
        TopicStatisticsRepository topicStatisticsRepository;

        @PersistenceContext
        EntityManager entityManager;

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

        /*
         * This query works fine, but IntelliJ doesn't like it for some reason.
         * This comment acts as a reminder on why the inspection is suppressed.
         */
        @SuppressWarnings("JpaQlInspection")
        @Override
        public Collection<Topic> getByNameContains(String name, Pageable pageable) {
            TypedQuery<Tuple> query = entityManager.createQuery(
                    """
                    select topic,
                    case when topic.name = :seq then 0
                         when topic.name like concat(:seq, '%') then 1
                         when topic.name like concat('%', :seq) then 3
                         else 2
                    end as score
                    from Topic topic
                    inner join topic.statistics
                    where topic.name like concat('%', :seq, '%')
                    order by
                        score,
                        topic.statistics.count desc,
                        topic.name
                    """,
                    Tuple.class
            );
            int limit = pageable.getPageSize();
            int offset = Math.toIntExact(pageable.getOffset());
            return query.setParameter("seq", name)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList()
                    .stream()
                    .map(this::convert)
                    .toList();
        }

        private Topic convert(Tuple tuple) {
            return tuple.get(0, Topic.class);
        }
    }
}
