package com.dabico.gseapp.converter;

import com.dabico.gseapp.dto.AccessTokenDto;
import com.dabico.gseapp.model.AccessToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccessTokenConverter {
    public List<AccessTokenDto> fromTokensToTokenDtos(List<AccessToken> tokens){
        return tokens.stream().map(this::fromTokenToTokenDto).collect(Collectors.toList());
    }

    public AccessTokenDto fromTokenToTokenDto(AccessToken token){
        return AccessTokenDto.builder().id(token.getId()).token(token.getToken()).build();
    }
}
