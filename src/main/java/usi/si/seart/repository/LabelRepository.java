package usi.si.seart.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.Label;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(@NotNull String name);

    @Query(
            "select l from Label l " +
            "join l.repos " +
            "group by l.id " +
            "order by COUNT(*) desc"
    )
    @Cacheable(value = "labels")
    List<Label> findMostFrequent(Pageable pageable);
}
