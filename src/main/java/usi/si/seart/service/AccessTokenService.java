package usi.si.seart.service;

import usi.si.seart.model.AccessToken;

import java.util.List;

public interface AccessTokenService {
    List<AccessToken> getAll();
    AccessToken create(AccessToken token);
    void delete(Long id);
}
