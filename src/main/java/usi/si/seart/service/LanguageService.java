package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import usi.si.seart.collection.ConcurrentLockMap;
import usi.si.seart.model.Language;
import usi.si.seart.repository.LanguageRepository;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

public interface LanguageService extends NamedEntityService<Language> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(onConstructor_ = @Autowired)
    class LanguageServiceImpl implements LanguageService {

        LanguageRepository languageRepository;

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
    }
}
