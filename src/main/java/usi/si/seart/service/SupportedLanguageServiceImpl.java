package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.repository.SupportedLanguageRepository;

import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class SupportedLanguageServiceImpl implements SupportedLanguageService {

    SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public List<SupportedLanguage> getAll(){
        return supportedLanguageRepository.findAll();
    }

    @Override
    public SupportedLanguage create(SupportedLanguage language){
        Optional<SupportedLanguage> opt = supportedLanguageRepository.findByName(language.getName());
        return opt.orElseGet(() -> supportedLanguageRepository.save(language));
    }
}
