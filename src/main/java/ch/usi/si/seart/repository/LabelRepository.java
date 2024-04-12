package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(@NotNull String name);

    @Query(
            value = """
            select label
            from Label label
            inner join label.statistics
            where label.name like concat('%', :seq, '%')
            order by
                case
                    when label.name = :seq then 0
                    when label.name like concat(:seq, '%') then 1
                    when label.name like concat('%', :seq) then 3
                    else 2
                end,
                label.statistics.count desc,
                label.name
            """,
            countQuery = """
            select count(label)
            from Label label
            where label.name like concat('%', :seq, '%')
            """
    )
    Page<Label> findAllByNameContainsOrderByBestMatch(@Param("seq") String name, Pageable pageable);
}
