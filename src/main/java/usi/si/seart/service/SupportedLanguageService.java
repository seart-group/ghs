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

import java.util.Collection;
import java.util.List;

public interface SupportedLanguageService extends EntityService<SupportedLanguage> {

    Collection<SupportedLanguage> getQueue();

    @Service
    @AllArgsConstructor(onConstructor_ = @Autowired)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class SupportedLanguageServiceImpl implements SupportedLanguageService {

        @NonFinal
        @Value("${app.crawl.languages}")
        List<String> languages;

        SupportedLanguageRepository supportedLanguageRepository;

        @Override
        public Collection<SupportedLanguage> getAll() {
            return supportedLanguageRepository.findAll();
        }

        @Override
        public Collection<SupportedLanguage> getQueue() {
            return supportedLanguageRepository.findAllByNameInOrderByCrawled(languages);
        }
    }
}
