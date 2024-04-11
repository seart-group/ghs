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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
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

        @PersistenceContext
        EntityManager entityManager;

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
        public Collection<Language> getAll(Pageable pageable) {
            return languageRepository.findAll(pageable).getContent();
        }

        /*
         * This query works fine, but IntelliJ doesn't like it for some reason.
         * This comment acts as a reminder on why the inspection is suppressed.
         */
        @SuppressWarnings("JpaQlInspection")
        @Override
        public Collection<Language> getByNameContains(String name, Pageable pageable) {
            TypedQuery<Tuple> query = entityManager.createQuery(
                    """
                    select language,
                    case when language.name = :seq then 0
                         when language.name like concat(:seq, '%') then 1
                         when language.name like concat('%', :seq) then 3
                         else 2
                    end as score
                    from Language language
                    left join language.statistics
                    where language.name like concat('%', :seq, '%')
                    order by
                        score,
                        language.statistics.mined desc nulls last,
                        language.name
                    """,
                    Tuple.class
            );
            int limit = pageable.getPageSize();
            int offset = Math.toIntExact(pageable.getOffset());
            return query.setParameter("seq", name)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList()
                    .stream()
                    .map(this::convert)
                    .toList();
        }

        private Language convert(Tuple tuple) {
            return tuple.get(0, Language.class);
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
