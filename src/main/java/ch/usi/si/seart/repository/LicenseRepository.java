package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.License;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, String> {

    Optional<License> findByName(@NotNull String name);

    @Query(
            value = """
            select license
            from License license
            inner join license.statistics
            where license.name like concat('%', :seq, '%')
            order by
                case
                    when license.name = :seq then 0
                    when license.name like concat(:seq, '%') then 1
                    when license.name like concat('%', :seq) then 3
                    else 2
                end,
                license.statistics.count desc,
                license.name
            """,
            countQuery = """
            select count(license)
            from License license
            where license.name like concat('%', :seq, '%')
            """
    )
    Page<License> findAllByNameContainsOrderByBestMatch(@Param("seq") String name, Pageable pageable);
}
