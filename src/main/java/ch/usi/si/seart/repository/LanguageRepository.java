package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByNameIgnoreCase(@NotNull String name);

    @Query(
        """
        select l from Language l
        inner join l.progress
        where l.name in (:names)
        order by l.progress.checkpoint
        """
    )
    List<Language> findAllByNameInOrderByProgress(Collection<@NotNull String> names);

    @Query(
            value = """
            select language
            from Language language
            left join language.statistics
            where language.name like concat('%', :seq, '%')
            order by
                case
                    when language.name = :seq then 0
                    when language.name like concat(:seq, '%') then 1
                    when language.name like concat('%', :seq) then 3
                    else 2
                end,
                language.statistics.mined desc nulls last,
                language.name
            """,
            countQuery = """
            select count(language)
            from Language language
            """
    )
    Page<Language> findAllByNameContainsOrderByBestMatch(@Param("seq") String name, Pageable pageable);
}
