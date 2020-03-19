package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.AccessTokenDtoList;
import com.dabico.gseapp.model.AccessToken;

public interface AccessTokenService {
    AccessTokenDtoList getAll();
    void create(AccessToken token);
    void delete(Long id);
}
