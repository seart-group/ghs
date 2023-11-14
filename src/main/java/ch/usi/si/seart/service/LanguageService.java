package ch.usi.si.seart.service;

import ch.usi.si.seart.collection.ConcurrentReadWriteLockMap;
import ch.usi.si.seart.config.properties.CrawlerProperties;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.LanguageProgressRepository;
import ch.usi.si.seart.repository.LanguageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

        CrawlerProperties crawlerProperties;

        LanguageRepository languageRepository;
        LanguageProgressRepository languageProgressRepository;

        ConcurrentReadWriteLockMap<String> locks = new ConcurrentReadWriteLockMap<>();

        @Override
        public Language getOrCreate(String name) {
            Lock readLock = locks.getReadLock(name);
            readLock.lock();
            try {
                return languageRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> create(name));
            } finally {
                readLock.unlock();
            }
        }

        private Language create(String name) {
            Lock writeLock = locks.getWriteLock(name);
            writeLock.lock();
            try {
                Language language = Language.builder().name(name).build();
                return languageRepository.save(language);
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public Collection<Language> getRanked() {
            return languageRepository.findAll(Sort.by("name"));
        }

        @Override
        public Collection<Language> getTargetedLanguages() {
            List<String> names = crawlerProperties.getLanguages();
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
