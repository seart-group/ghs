package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, String> {

    Optional<License> findByName(@NotNull String name);
}
