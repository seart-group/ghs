package ch.usi.si.seart.service;

import ch.usi.si.seart.config.properties.CrawlerProperties;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.repository.LanguageProgressRepository;
import ch.usi.si.seart.repository.LanguageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(onConstructor_ = @Autowired)
    class LanguageServiceImpl implements LanguageService, InitializingBean {

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
        public Page<Language> getAll(Pageable pageable) {
            return languageRepository.findAll(pageable);
        }

        @Override
        public Page<Language> getByNameContains(String name, Pageable pageable) {
            return languageRepository.findAllByNameContainsOrderByBestMatch(
                    name, PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            );
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

        @Override
        public void afterPropertiesSet() {
            Date defaultCheckpoint = crawlerProperties.getStartDate();
            boolean enabled = Boolean.TRUE.equals(crawlerProperties.getEnabled());
            List<String> names = crawlerProperties.getLanguages();
            if (!enabled || names.isEmpty()) return;
            log.info("Validating mining progress for {} languages...", names.size());
            for (String name : names) {
                Language language = languageRepository.findByNameIgnoreCase(name)
                        .orElseGet(() ->
                                languageRepository.save(
                                        Language.builder()
                                                .name(name)
                                                .build()
                                )
                        );
                languageProgressRepository.findByLanguage(language)
                        .ifPresentOrElse(
                                progress -> log.debug(
                                        "{} repositories crawled up to: {}",
                                        name, progress.getCheckpoint()
                                ),
                                () -> {
                                    log.debug("Initializing progress for {} to default start date...", name);
                                    Language.Progress progress = Language.Progress.builder()
                                            .id(language.getId())
                                            .language(language)
                                            .checkpoint(defaultCheckpoint)
                                            .build();
                                    Language.Statistics statistics = Language.Statistics.builder()
                                            .id(language.getId())
                                            .language(language)
                                            .build();
                                    language.setProgress(progress);
                                    language.setStatistics(statistics);
                                    languageRepository.save(language);
                                });
            }
        }
    }
}
