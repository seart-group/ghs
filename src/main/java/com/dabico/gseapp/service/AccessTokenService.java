package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.dto.AccessTokenDtoList;

public interface AccessTokenService {
    AccessTokenDtoList getAll();
    void createOrUpdate(AccessTokenDto atdto);
    void delete(Long id);
}
