package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.model.AccessToken;

import java.util.List;

public interface AccessTokenService {
    List<AccessToken> getAll();
    AccessToken create(AccessToken token);
    void delete(Long id);
}
