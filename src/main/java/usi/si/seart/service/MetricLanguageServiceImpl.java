package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;
import usi.si.seart.model.MetricLanguage;
import usi.si.seart.repository.MetricLanguageRepository;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MetricLanguageServiceImpl implements MetricLanguageService{

    ConcurrentReferenceHashMap<String, Object> metricLanguageLocks = new ConcurrentReferenceHashMap<>();

    MetricLanguageRepository metricLanguageRepository;

    private Object getMetricLanguageLock(String language_name) {
        return this.metricLanguageLocks.compute(language_name, (k, v) -> v == null ? new Object() : v);
    }

    public MetricLanguage getOrCreateMetricLanguage(@NotNull String language_name) {
        return metricLanguageRepository.findByLanguage(language_name).orElseGet(() -> {
            // Acquires the lock for the metric language name
            synchronized (getMetricLanguageLock(language_name)) {
                // Checks whether the metric language entity has been created while awaiting the lock
                return metricLanguageRepository.findByLanguage(language_name).orElseGet(() ->
                        // If not, creates it.
                        metricLanguageRepository.save(
                            MetricLanguage.builder()
                                    .language(language_name)
                                    .build()));
            }
        });
    }
}