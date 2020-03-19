package com.dabico.gseapp.service;

import com.dabico.gseapp.converter.AccessTokenConverter;
import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.dto.AccessTokenDtoList;
import com.dabico.gseapp.model.AccessToken;
import com.dabico.gseapp.repository.AccessTokenRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AccessTokenServiceImpl implements AccessTokenService {
    AccessTokenRepository accessTokenRepository;
    AccessTokenConverter accessTokenConverter;

    @Override
    public AccessTokenDtoList getAll(){
        List<AccessToken> tokens = accessTokenRepository.findAll();
        List<AccessTokenDto> dtos = accessTokenConverter.fromTokensToTokenDtos(tokens);
        return AccessTokenDtoList.builder().items(dtos).build();
    }

    @Override
    public void create(AccessToken token){
        Optional<AccessToken> opt = accessTokenRepository.findByValue(token.getValue());
        if (opt.isEmpty()){
            accessTokenRepository.save(token);
        }
    }

    @Override
    public void delete(Long id){ accessTokenRepository.deleteById(id); }
}
