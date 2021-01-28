package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.dto.AccessTokenDtoList;
import usi.si.seart.gseapp.model.AccessToken;

public interface AccessTokenService {
    AccessTokenDtoList getAll();
    AccessTokenDto create(AccessToken token);
    void delete(Long id);
}
