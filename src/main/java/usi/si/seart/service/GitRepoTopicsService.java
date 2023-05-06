package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.Topic;
import usi.si.seart.repository.GitRepoTopicRepository;
import usi.si.seart.repository.TopicRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface GitRepoTopicsService {

    Topic getOrCreateTopic(@NotNull String languageName);

    void createOrUpdateGitRepoTopics(@NotNull GitRepo repo, @NotNull List<GitRepoTopic> topics);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    @PersistenceContext
    class GitRepoTopicsServiceImpl implements GitRepoTopicsService {

        EntityManager entityManager;

        EntityManagerFactory entityManagerFactory;

        ConcurrentReferenceHashMap<String, Object> tagLocks = new ConcurrentReferenceHashMap<>();

        TopicRepository topicRepository;
        GitRepoTopicRepository gitRepoTopicRepository;

        private Object getRepoTagLock(String languageName) {
            return this.tagLocks.compute(languageName, (k, v) -> v == null ? new Object() : v);
        }

        public Topic getOrCreateTopic(@NotNull String label) {
            return topicRepository.findByLabel(label).orElseGet(() -> {
                // Acquires the lock for the repo topic label
                synchronized (getRepoTagLock(label)) {
                    // Checks whether the repo topic entity has been created while awaiting the lock
                    Topic repoTopic = topicRepository.findByLabel(label).orElseGet(() ->
                            // If not, creates it.
                            topicRepository.save(
                                    Topic.builder()
                                            .label(label)
                                            .build()));
                    entityManager.clear();
                    entityManagerFactory.getCache().evict(Topic.class, repoTopic.getId());
                    return repoTopic;
                }
            });
        }

        @Override
        public void createOrUpdateGitRepoTopics(GitRepo repo, List<GitRepoTopic> topics) {
            gitRepoTopicRepository.deleteAllByRepo(repo);
            gitRepoTopicRepository.saveAll(topics);
        }
    }
}
