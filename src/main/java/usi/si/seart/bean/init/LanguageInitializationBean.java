package usi.si.seart.bean.init;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import usi.si.seart.model.Language;
import usi.si.seart.repository.LanguageProgressRepository;
import usi.si.seart.repository.LanguageRepository;

import java.util.Date;
import java.util.List;

@Slf4j
@Component("LanguageInitializationBean")
@ConditionalOnExpression(value = "${app.crawl.enabled:false} and not '${app.crawl.languages}'.isBlank()")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LanguageInitializationBean implements InitializingBean {

    @NonFinal
    @Value("${app.crawl.languages}")
    List<String> names;

    @NonFinal
    @Value(value = "${app.crawl.start-date}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    Date defaultStartDate;

    LanguageRepository languageRepository;
    LanguageProgressRepository languageProgressRepository;

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
