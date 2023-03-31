package usi.si.seart.bean;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import usi.si.seart.model.SupportedLanguage;
import usi.si.seart.repository.SupportedLanguageRepository;

import java.util.List;

@Slf4j
@Component("SupportedLanguageInitializationBean")
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportedLanguageInitializationBean implements InitializingBean {

    @Value("${app.crawl.languages}")
    List<String> languages;

    final SupportedLanguageRepository supportedLanguageRepository;

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing supported languages...");
        languages.forEach(language -> {
            boolean initialized = supportedLanguageRepository.existsByName(language);
            if (!initialized) {
                log.debug("\tCreating database entry for language: \"{}\"...", language);
                SupportedLanguage supportedLanguage = SupportedLanguage.builder().name(language).build();
                supportedLanguageRepository.save(supportedLanguage);
            } else {
                log.debug("\tSkipping already initialized language: \"{}\"...", language);
            }
        });
        log.info("Initialized {} languages!", languages.size());
    }
}
