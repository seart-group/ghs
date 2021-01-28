package usi.si.seart.gseapp.converter;

import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.model.AccessToken;
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
        return AccessTokenDto.builder().id(token.getId()).value(token.getValue()).build();
    }

    public AccessToken fromTokenDtoToToken(AccessTokenDto dto){
        return AccessToken.builder().id(dto.getId()).value(dto.getValue()).build();
    }
}
