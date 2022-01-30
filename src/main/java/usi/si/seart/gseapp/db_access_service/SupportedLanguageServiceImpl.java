package usi.si.seart.gseapp.db_access_service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.model.SupportedLanguage;
import usi.si.seart.gseapp.repository.SupportedLanguageRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class SupportedLanguageServiceImpl implements SupportedLanguageService {
    SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public List<String> getAll(){
        List<SupportedLanguage> languages = supportedLanguageRepository.findAll();
        return languages.stream().map(SupportedLanguage::getName).collect(Collectors.toList());
    }

    @Override
    public SupportedLanguage create(SupportedLanguage language){
        Optional<SupportedLanguage> opt = supportedLanguageRepository.findByName(language.getName());
        return opt.orElseGet(() -> supportedLanguageRepository.save(language));
    }

    @Override
    public void delete(Long id){
        supportedLanguageRepository.deleteById(id);
    }
}
