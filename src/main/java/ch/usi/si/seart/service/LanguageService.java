package ch.usi.si.seart.service;

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
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        @Override
        public Language getOrCreate(String name) {
            Language language;
            readWriteLock.readLock().lock();

            try {
                Optional<Language> optional = languageRepository.findByNameIgnoreCase(name);
                if (optional.isEmpty()) {
                    readWriteLock.readLock().unlock();
                    readWriteLock.writeLock().lock();
                    try {
                        language = languageRepository.save(
                                Language.builder()
                                        .name(name)
                                        .build()
                        );
                        readWriteLock.readLock().lock();
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }
                } else {
                    language = optional.get();
                }
            } finally {
                readWriteLock.readLock().unlock();
            }

            return language;
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
            Language.Progress progress = languageProgressRepository.findByLanguage(language)
                            .orElseGet(
                                    () -> Language.Progress.builder()
                                            .id(language.getId())
                                            .language(language)
                                            .build()
                            );
            progress.setCheckpoint(checkpoint);
            languageProgressRepository.save(progress);
        }
    }
}
