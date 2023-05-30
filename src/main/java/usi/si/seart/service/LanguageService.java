package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import usi.si.seart.model.Language;
import usi.si.seart.repository.LanguageRepository;

import java.util.Collection;

public interface LanguageService extends NamedEntityService<Language> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LanguageServiceImpl implements LanguageService {

        LanguageRepository languageRepository;

        @Override
        public Language getOrCreate(String name) {
            return languageRepository.findByName(name)
                    .orElseGet(() -> languageRepository.save(
                            Language.builder()
                                    .name(name)
                                    .build()
                    ));
        }

        @Override
        public Collection<Language> getRanked() {
            return languageRepository.findAll(Sort.by("name"));
        }
    }
}
