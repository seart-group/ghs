package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.repository.GitRepoRepository;
import usi.si.seart.service.StaticCodeAnalysisService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@Service
@ConditionalOnProperty(value = "app.crawl.cloning.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeAnalysisJob {

    StaticCodeAnalysisService staticCodeAnalysisService;

    GitRepoRepository gitRepoRepository;

    @Transactional
    @Scheduled(fixedDelayString = "${app.crawl.cloning.scheduling}")
    public void run() {
        final long outdatedRepos = gitRepoRepository.countAllRepoWithOutdatedCodeMetrics();
        final long allRepos = gitRepoRepository.count();

        log.info("CodeAnalysis job started on {}/{} repositories", outdatedRepos, allRepos);
        gitRepoRepository.findAllRepoWithOutdatedCodeMetrics().forEach(this::analyze);
    }

    @Transactional(propagation = Propagation.NESTED)
    public Future<Set<GitRepoMetric>> analyze(GitRepo repo) {
        try {
            return staticCodeAnalysisService.getCodeMetrics(repo, true);
        } catch (StaticCodeAnalysisException e) {
            log.error("Error during code analysis job", e);
            return CompletableFuture.completedFuture(new HashSet<>());
        }
    }
}
