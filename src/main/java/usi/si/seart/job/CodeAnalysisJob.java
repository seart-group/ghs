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
import usi.si.seart.analysis.StaticCodeAnalyzer;
import usi.si.seart.repository.GitRepoRepository;

/**
 * Mines code metrics for every needed repository.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.crawl.analysis.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeAnalysisJob {

    GitRepoRepository gitRepoRepository;

    StaticCodeAnalyzer staticCodeAnalyzer;

    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${app.crawl.analysis.scheduling}")
    public void run() {
        long total = gitRepoRepository.count();
        long outdated = gitRepoRepository.countAllRepoWithOutdatedCodeMetrics();
        log.info("Started analysis on {}/{} repositories", outdated, total);
        gitRepoRepository.findAllRepoWithOutdatedCodeMetrics().forEach(this::analyze);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyze(String name) {
        staticCodeAnalyzer.gatherCodeMetricsFor(name);
    }
}
