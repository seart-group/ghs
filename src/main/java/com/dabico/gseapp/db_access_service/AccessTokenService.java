package com.dabico.gseapp.db_access_service;

import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.dto.AccessTokenDtoList;
import com.dabico.gseapp.model.AccessToken;

public interface AccessTokenService {
    AccessTokenDtoList getAll();
    AccessTokenDto create(AccessToken token);
    void delete(Long id);
}
