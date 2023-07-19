package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.Language;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByNameIgnoreCase(@NotNull String name);

    @Query(
            "select l from Language l " +
            "inner join l.progress " +
            "where l.name in (:names) " +
            "order by l.progress.checkpoint"
    )
    List<Language> findAllByNameInOrderByProgress(Collection<@NotNull String> names);
}
