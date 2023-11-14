package ch.usi.si.seart.bean.init;

import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.LanguageProgressRepository;
import ch.usi.si.seart.repository.LanguageRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component("LanguageInitializationBean")
@ConditionalOnExpression(value = "${app.crawl.enabled:false} and not '${app.crawl.languages}'.isBlank()")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LanguageInitializationBean implements InitializingBean {

    List<String> names;

    Date defaultStartDate;

    LanguageRepository languageRepository;
    LanguageProgressRepository languageProgressRepository;

    @Autowired
    public LanguageInitializationBean(
            @Value("${app.crawl.languages}")
            List<String> names,
            @Value(value = "${app.crawl.start-date}")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
            Date defaultStartDate,
            LanguageRepository languageRepository,
            LanguageProgressRepository languageProgressRepository
    ) {
        this.names = names;
        this.defaultStartDate = defaultStartDate;
        this.languageRepository = languageRepository;
        this.languageProgressRepository = languageProgressRepository;
    }

    @Override
    public void afterPropertiesSet() {
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
                                        .checkpoint(defaultStartDate)
                                        .build()
                        );
                    });
        }
        log.info("Successfully validated progress for {} languages!", names.size());
    }
}
