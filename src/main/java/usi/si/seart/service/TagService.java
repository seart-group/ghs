package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;
import usi.si.seart.model.Tag;
import usi.si.seart.repository.TagRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

public interface TagService {

    Tag getOrCreateTag(@NotNull String languageName);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    @PersistenceContext
    class TagServiceImpl implements TagService {

        EntityManager entityManager;

        EntityManagerFactory entityManagerFactory;

        ConcurrentReferenceHashMap<String, Object> tagLocks = new ConcurrentReferenceHashMap<>();

        TagRepository tagRepository;

        private Object getRepoTagLock(String languageName) {
            return this.tagLocks.compute(languageName, (k, v) -> v == null ? new Object() : v);
        }

        public Tag getOrCreateTag(@NotNull String label) {
            return tagRepository.findByLabel(label).orElseGet(() -> {
                // Acquires the lock for the repo tag label
                synchronized (getRepoTagLock(label)) {
                    // Checks whether the repo tag entity has been created while awaiting the lock
                    Tag repoTag = tagRepository.findByLabel(label).orElseGet(() ->
                            // If not, creates it.
                            tagRepository.save(
                                    Tag.builder()
                                            .label(label)
                                            .build()));
                    entityManager.clear();
                    entityManagerFactory.getCache().evict(Tag.class, repoTag.getId());
                    return repoTag;
                }
            });
        }
    }
}
