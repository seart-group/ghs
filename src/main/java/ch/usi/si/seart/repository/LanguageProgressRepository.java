package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;

public interface LanguageProgressRepository extends JpaRepository<Language.Progress, Long> {

    Optional<Language.Progress> findByLanguage(@NotNull Language language);
}
