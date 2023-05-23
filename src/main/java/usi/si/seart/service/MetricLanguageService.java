package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;
import usi.si.seart.model.MetricLanguage;
import usi.si.seart.repository.MetricLanguageRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

public interface MetricLanguageService {

    MetricLanguage getOrCreateMetricLanguage(@NotNull String languageName);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    @PersistenceContext
    class MetricLanguageServiceImpl implements MetricLanguageService {

        EntityManager entityManager;

        EntityManagerFactory entityManagerFactory;

        ConcurrentReferenceHashMap<String, Object> metricLanguageLocks = new ConcurrentReferenceHashMap<>();

        MetricLanguageRepository metricLanguageRepository;

        private Object getMetricLanguageLock(String languageName) {
            return this.metricLanguageLocks.compute(languageName, (k, v) -> v == null ? new Object() : v);
        }

        public MetricLanguage getOrCreateMetricLanguage(@NotNull String languageName) {
            return metricLanguageRepository.findByLanguage(languageName).orElseGet(() -> {
                // Acquires the lock for the metric language name
                synchronized (getMetricLanguageLock(languageName)) {
                    // Checks whether the metric language entity has been created while awaiting the lock
                    MetricLanguage metricLang = metricLanguageRepository.findByLanguage(languageName).orElseGet(() ->
                            // If not, creates it.
                            metricLanguageRepository.save(
                                    MetricLanguage.builder()
                                            .language(languageName)
                                            .build()));
                    entityManager.clear();
                    entityManagerFactory.getCache().evict(MetricLanguage.class, metricLang.getId());
                    return metricLang;

                }
            });
        }
    }

}