package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.Language;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface LanguageProgressRepository extends JpaRepository<Language.Progress, Long> {

    Optional<Language.Progress> findByLanguage(@NotNull Language language);
}
