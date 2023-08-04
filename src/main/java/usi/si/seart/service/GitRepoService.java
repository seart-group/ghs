package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.GitRepoLabelRepository;
import usi.si.seart.repository.GitRepoLanguageRepository;
import usi.si.seart.repository.GitRepoMetricRepository;
import usi.si.seart.repository.GitRepoRepository;
import usi.si.seart.repository.GitRepoTopicRepository;
import usi.si.seart.repository.specification.GitRepoSearch;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.stream.Stream;

public interface GitRepoService {

    @Retryable(
            value = TransientDataAccessException.class,
            backoff = @Backoff(delay = 250, multiplier = 2),
            maxAttempts = 5
    )
    void deleteRepoById(Long id);
    Long count();
    Long countAnalysisCandidates();
    GitRepo getRepoById(Long id);
    GitRepo getByName(String name);
    @Retryable(
            value = TransientDataAccessException.class,
            backoff = @Backoff(delay = 250, multiplier = 2),
            maxAttempts = 5
    )
    GitRepo createOrUpdate(GitRepo repo);
    Optional<GitRepo> getNextDeletionCandidate();
    Page<GitRepo> findDynamically(GitRepoSearch parameters, Pageable pageable);
    Stream<GitRepo> streamDynamically(GitRepoSearch parameters);
    Stream<Pair<Long, String>> streamAnalysisCandidates();

    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class GitRepoServiceImpl implements GitRepoService {

        GitRepoRepository gitRepoRepository;
        GitRepoLabelRepository gitRepoLabelRepository;
        GitRepoLanguageRepository gitRepoLanguageRepository;
        GitRepoMetricRepository gitRepoMetricRepository;
        GitRepoTopicRepository gitRepoTopicRepository;

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void deleteRepoById(Long id) {
            gitRepoLabelRepository.deleteByRepoId(id);
            gitRepoLanguageRepository.deleteByRepoId(id);
            gitRepoMetricRepository.deleteByRepoId(id);
            gitRepoTopicRepository.deleteByRepoId(id);
            gitRepoRepository.deleteById(id);
        }

        @Override
        public Long count() {
            return gitRepoRepository.count();
        }

        @Override
        public Long countAnalysisCandidates() {
            return gitRepoRepository.countWithOutdatedCodeMetrics();
        }

        @Override
        public GitRepo getRepoById(Long id) {
            return gitRepoRepository.findGitRepoById(id)
                    .orElseThrow(EntityNotFoundException::new);
        }

        @Override
        public GitRepo getByName(String name) {
            return gitRepoRepository.findGitRepoByNameIgnoreCase(name)
                    .orElseThrow(EntityNotFoundException::new);
        }

        @Override
        public GitRepo createOrUpdate(GitRepo repo) {
            return gitRepoRepository.save(repo);
        }

        @Override
        public Optional<GitRepo> getNextDeletionCandidate() {
            Pageable pageable = PageRequest.of(0, 1);
            Page<GitRepo> page = gitRepoRepository.findGitRepoByOrderByLastPinged(pageable);
            return page.stream().findFirst();
        }

        @Override
        public Page<GitRepo> findDynamically(GitRepoSearch parameters, Pageable pageable) {
            return gitRepoRepository.findAllDynamically(parameters, pageable);
        }

        @Override
        public Stream<GitRepo> streamDynamically(GitRepoSearch parameters) {
            return gitRepoRepository.streamAllDynamically(parameters);
        }

        @Override
        public Stream<Pair<Long, String>> streamAnalysisCandidates() {
            return gitRepoRepository.streamIdentifiersWithOutdatedCodeMetrics()
                    .map(tuple -> Pair.of(
                            tuple.get("id", Long.class),
                            tuple.get("name", String.class)
                    ));
        }
    }
}
