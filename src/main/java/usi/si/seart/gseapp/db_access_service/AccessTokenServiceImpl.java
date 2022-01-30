package usi.si.seart.gseapp.db_access_service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.model.AccessToken;
import usi.si.seart.gseapp.repository.AccessTokenRepository;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class AccessTokenServiceImpl implements AccessTokenService {
    AccessTokenRepository accessTokenRepository;

    @Override
    public List<AccessToken> getAll(){
        return accessTokenRepository.findAll();
    }

    @Override
    public AccessToken create(AccessToken token){
        Optional<AccessToken> opt = accessTokenRepository.findByValue(token.getValue());
        return opt.orElseGet(() -> accessTokenRepository.save(token));
    }

    @Override
    public void delete(Long id){
        accessTokenRepository.deleteById(id);
    }
}
