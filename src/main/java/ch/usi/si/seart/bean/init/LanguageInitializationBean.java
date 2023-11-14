package ch.usi.si.seart.bean.init;

import ch.usi.si.seart.config.properties.CrawlerProperties;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.LanguageProgressRepository;
import ch.usi.si.seart.repository.LanguageRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("LanguageInitializationBean")
@ConditionalOnExpression(value = "${ghs.crawler.enabled:false} and not '${ghs.crawler.languages}'.isBlank()")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class LanguageInitializationBean implements InitializingBean {

    CrawlerProperties crawlerProperties;

    LanguageRepository languageRepository;
    LanguageProgressRepository languageProgressRepository;

    @Override
    public void afterPropertiesSet() {
        List<String> names = crawlerProperties.getLanguages();
        for (String name : names) {
            Language language = languageRepository.findByNameIgnoreCase(name)
                    .map(existing -> {
                        log.debug("\t   Found database entry for language: \"{}\"", name);
                        return existing;
                    })
                    .orElseGet(() -> {
                        log.debug("\tCreating database entry for language: \"{}\"", name);
                        return languageRepository.save(
                                Language.builder()
                                        .name(name)
                                        .build()
                        );
                    });
            languageProgressRepository.findByLanguage(language)
                    .map(existing -> {
                        log.debug("\tFound progress for \"{}\"", name);
                        return existing;
                    })
                    .orElseGet(() -> {
                        log.debug("\t   No progress for \"{}\", initializing to default start date", name);
                        return languageProgressRepository.save(
                                Language.Progress.builder()
                                        .language(language)
                                        .checkpoint(crawlerProperties.getStartDate())
                                        .build()
                        );
                    });
        }
        log.info("Successfully validated progress for {} languages!", names.size());
    }
}
