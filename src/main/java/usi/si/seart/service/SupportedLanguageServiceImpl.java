package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.repository.SupportedLanguageRepository;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportedLanguageServiceImpl implements SupportedLanguageService {

    @Value("${app.crawl.languages}")
    List<String> languages;

    final SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public List<SupportedLanguage> getAll(){
        return supportedLanguageRepository.findAll();
    }

    @Override
    public List<SupportedLanguage> getQueue() {
        return supportedLanguageRepository.findAllByNameInOrderByCrawled(languages);
    }
}
