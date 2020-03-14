package com.dabico.gseapp.service;

import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.model.AccessToken;
import com.dabico.gseapp.repository.AccessTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {
    AccessTokenRepository accessTokenRepository;

    @Override
    public void createOrUpdate(AccessTokenDto atdto){
        AccessToken at = AccessToken.builder().build();
        if (atdto.getId() != null){
            at = accessTokenRepository.findById(atdto.getId()).get();
        }
        at.setToken(atdto.getToken());
        accessTokenRepository.save(at);
    }

    @Override
    public void delete(Long id){ accessTokenRepository.deleteById(id); }
}
