package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import usi.si.seart.collection.ConcurrentLockMap;
import usi.si.seart.model.Language;
import usi.si.seart.repository.LanguageProgressRepository;
import usi.si.seart.repository.LanguageRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

public interface LanguageService extends NamedEntityService<Language> {

    Collection<Language> getTargetedLanguages();
    Language.Progress getProgress(Language language);
    void updateProgress(Language language, Date checkpoint);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(onConstructor_ = @Autowired)
    class LanguageServiceImpl implements LanguageService {

        @NonFinal
        @Value("${app.crawl.languages}")
        List<String> names;

        LanguageRepository languageRepository;
        LanguageProgressRepository languageProgressRepository;

        ConcurrentLockMap<String> locks = new ConcurrentLockMap<>();

        @Override
        public Language getOrCreate(String name) {
            return languageRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Lock lock = locks.get(name);
                        lock.lock();
                        try {
                            return languageRepository.findByNameIgnoreCase(name)
                                    .orElseGet(() -> languageRepository.save(Language.builder().name(name).build()));
                        } finally {
                            lock.unlock();
                        }
                    });
        }

        @Override
        public Collection<Language> getRanked() {
            return languageRepository.findAll(Sort.by("name"));
        }

        @Override
        public Collection<Language> getTargetedLanguages() {
            return languageRepository.findAllByNameInOrderByProgress(names);
        }

        @Override
        public Language.Progress getProgress(Language language) {
            return languageProgressRepository.findByLanguage(language)
                    .orElseThrow(EntityNotFoundException::new);
        }

        @Override
        public void updateProgress(Language language, Date checkpoint) {
            languageProgressRepository.findByLanguage(language)
                    .ifPresent(progress -> {
                        progress.setCheckpoint(checkpoint);
                        languageProgressRepository.save(progress);
                    });
        }
    }
}
