package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.repository.SupportedLanguageRepository;

import java.util.List;

public interface SupportedLanguageService {

    List<SupportedLanguage> getAll();
    List<SupportedLanguage> getQueue();

    @Service
    @AllArgsConstructor(onConstructor_ = @Autowired)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class SupportedLanguageServiceImpl implements SupportedLanguageService {

        @NonFinal
        @Value("${app.crawl.languages}")
        List<String> languages;

        SupportedLanguageRepository supportedLanguageRepository;

        @Override
        public List<SupportedLanguage> getAll(){
            return supportedLanguageRepository.findAll();
        }

        @Override
        public List<SupportedLanguage> getQueue() {
            return supportedLanguageRepository.findAllByNameInOrderByCrawled(languages);
        }
    }
}
