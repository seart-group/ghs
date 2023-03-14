package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.repository.MetricLanguageRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MetricLanguageServiceImpl implements MetricLanguageService{

    MetricLanguageRepository metricLanguageRepository;

    public Optional<Long> getLanguageId(@NotNull String language_name) {
        return metricLanguageRepository.findLanguageIdByLanguage(language_name);
    }
}