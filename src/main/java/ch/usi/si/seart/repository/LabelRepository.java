package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(@NotNull String name);
}
