package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.converter.AccessTokenConverter;
import usi.si.seart.gseapp.dto.AccessTokenDto;
import usi.si.seart.gseapp.dto.AccessTokenDtoList;
import usi.si.seart.gseapp.model.AccessToken;
import usi.si.seart.gseapp.repository.AccessTokenRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    public AccessTokenDto create(AccessToken token){
        Optional<AccessToken> opt = accessTokenRepository.findByValue(token.getValue());
        if (opt.isEmpty()){
            return accessTokenConverter.fromTokenToTokenDto(accessTokenRepository.save(token));
        }
        return null;
    }

    @Override
    public void delete(Long id){ accessTokenRepository.deleteById(id); }
}
