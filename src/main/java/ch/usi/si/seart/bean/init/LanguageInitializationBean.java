package ch.usi.si.seart.bean.init;

import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.service.LanguageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LanguageInitializationBean implements InitializingBean {

    List<String> names;

    Date startDate;

    @NonFinal
    @Accessors(makeFinal = true)
    @Setter(onMethod_ = @Autowired)
    LanguageService languageService;

    @Override
    public void afterPropertiesSet() {
        if (names.isEmpty()) return;
        log.info("Validating mining progress for {} languages...", names.size());
        for (String name : names) {
            Language language = languageService.getOrCreate(name);
            try {
                languageService.getProgress(language);
            } catch (EntityNotFoundException ignored) {
                log.debug("No progress found for \"{}\", initializing to default start date...", name);
                languageService.updateProgress(language, startDate);
            }
        }
    }
}
