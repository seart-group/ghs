package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.AccessTokenDto;

public interface AccessTokenService {
    void createOrUpdate(AccessTokenDto atdto);
    void delete(Long id);
}
