package com.dabico.gseapp.service;

import com.dabico.gseapp.model.AccessToken;

public interface AccessTokenService {
    AccessToken create(AccessToken at);
    void delete(AccessToken at);
}
