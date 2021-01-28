package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.converter.SupportedLanguageConverter;
import usi.si.seart.gseapp.dto.StringList;
import usi.si.seart.gseapp.dto.SupportedLanguageDto;
import usi.si.seart.gseapp.model.SupportedLanguage;
import usi.si.seart.gseapp.repository.SupportedLanguageRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class SupportedLanguageServiceImpl implements SupportedLanguageService {
    SupportedLanguageRepository supportedLanguageRepository;
    SupportedLanguageConverter supportedLanguageConverter;

    @Override
    public StringList getAll(){
        List<SupportedLanguage> languages = supportedLanguageRepository.findAll();
        List<String> languagesNames = new ArrayList<>();
        languages.forEach(language -> languagesNames.add(language.getName()));
        return StringList.builder().items(languagesNames).build();
    }

    @Override
    public SupportedLanguageDto create(SupportedLanguage language){
        Optional<SupportedLanguage> opt = supportedLanguageRepository.findByName(language.getName());
        if (opt.isEmpty()){
            return supportedLanguageConverter.fromLanguageToLanguageDto(supportedLanguageRepository.save(language));
        }
        return null;
    }

    @Override
    public void delete(Long id){ supportedLanguageRepository.deleteById(id); }
}
