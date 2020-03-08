package com.dabico.gseapp.repository;

import com.dabico.gseapp.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepository extends JpaRepository<AccessToken,Long> {
}
