package usi.si.seart.gseapp.repository;

import usi.si.seart.gseapp.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken,Long> {
    Optional<AccessToken> findByValue(String value);
}
