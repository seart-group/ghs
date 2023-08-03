package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.Label;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(@NotNull String name);

    Collection<Label> findAllByNameIn(Collection<@NotNull String> names);
}
