package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import usi.si.seart.model.GitRepo;
import usi.si.seart.repository.GitRepoRepository;
import usi.si.seart.repository.specification.GitRepoSearch;

import javax.persistence.EntityNotFoundException;
import java.util.stream.Stream;

public interface GitRepoService {

    Long count();
    GitRepo getRepoById(Long id);
    GitRepo getByName(String name);
    @Retryable(
            value = TransientDataAccessException.class,
            backoff = @Backoff(delay = 250, multiplier = 2),
            maxAttempts = 5
    )
    GitRepo createOrUpdateRepo(GitRepo repo);
    @Retryable(
            value = TransientDataAccessException.class,
            backoff = @Backoff(delay = 250, multiplier = 2),
            maxAttempts = 5
    )
    GitRepo updateRepo(GitRepo repo);
    Page<GitRepo> findDynamically(GitRepoSearch parameters, Pageable pageable);
    Stream<GitRepo> streamDynamically(GitRepoSearch parameters);

    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class GitRepoServiceImpl implements GitRepoService {

        GitRepoRepository gitRepoRepository;

        @Override
        public Long count() {
            return gitRepoRepository.count();
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
        public GitRepo createOrUpdateRepo(GitRepo repo) {
            String name = repo.getName();
            Long id = gitRepoRepository.findGitRepoByNameIgnoreCase(name)
                    .map(GitRepo::getId)
                    .orElse(null);
            repo.setId(id);
            return gitRepoRepository.save(repo);
        }

        @Override
        public GitRepo updateRepo(GitRepo repo) {
            return gitRepoRepository.save(repo);
        }

        @Override
        public Page<GitRepo> findDynamically(GitRepoSearch parameters, Pageable pageable) {
            return gitRepoRepository.findAllDynamically(parameters, pageable);
        }

        @Override
        public Stream<GitRepo> streamDynamically(GitRepoSearch parameters) {
            return gitRepoRepository.streamAllDynamically(parameters);
        }
    }
}
