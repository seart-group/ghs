package ch.usi.si.seart.service;

import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.repository.LabelRepository;
import ch.usi.si.seart.repository.LabelStatisticsRepository;
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

public interface LabelService extends NamedEntityService<Label> {

    Collection<Label> getByNameContains(String name, Pageable pageable);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LabelServiceImpl implements LabelService {

        LabelRepository labelRepository;
        LabelStatisticsRepository labelStatisticsRepository;

        @PersistenceContext
        EntityManager entityManager;

        @Override
        public Label getOrCreate(@NotNull String name) {
            return labelRepository.findByName(name)
                    .orElseGet(() -> labelRepository.save(
                            Label.builder()
                                    .name(name)
                                    .build()
                    ));
        }

        @Override
        public Collection<Label> getAll(Pageable pageable) {
            Pageable adjusted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.Direction.DESC,
                    Label.Statistics_.COUNT
            );
            return labelStatisticsRepository.findAll(adjusted).stream()
                    .map(Label.Statistics::getLabel)
                    .toList();
        }

        /*
         * This query works fine, but IntelliJ doesn't like it for some reason.
         * This comment acts as a reminder on why the inspection is suppressed.
         */
        @SuppressWarnings("JpaQlInspection")
        @Override
        public Collection<Label> getByNameContains(String name, Pageable pageable) {
            TypedQuery<Tuple> query = entityManager.createQuery(
                    """
                    select label,
                    case when label.name = :seq then 0
                         when label.name like concat(:seq, '%') then 1
                         when label.name like concat('%', :seq) then 3
                         else 2
                    end as score
                    from Label label
                    inner join label.statistics
                    where label.name like concat('%', :seq, '%')
                    order by
                        score,
                        label.statistics.count desc,
                        label.name
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

        private Label convert(Tuple tuple) {
            return tuple.get(0, Label.class);
        }
    }
}
