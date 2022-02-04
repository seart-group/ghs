package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.gseapp.model.AccessToken;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByValue(String value);
}
