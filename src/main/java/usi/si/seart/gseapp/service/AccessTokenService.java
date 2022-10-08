package usi.si.seart.gseapp.service;

import usi.si.seart.gseapp.model.AccessToken;

import java.util.List;

public interface AccessTokenService {
    List<AccessToken> getAll();
    AccessToken create(AccessToken token);
    void delete(Long id);
}
