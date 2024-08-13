package ch.usi.si.seart.job;

import ch.usi.si.seart.cloc.CLOC;
import ch.usi.si.seart.cloc.CLOCException;
import ch.usi.si.seart.config.properties.AnalysisProperties;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.io.TemporaryDirectory;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.join.GitRepoMetric;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.service.LanguageService;
import ch.usi.si.seart.stereotype.Job;
import ch.usi.si.seart.util.Optionals;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Job
@Slf4j
@ConditionalOnProperty(value = "ghs.analysis.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeAnalysisJob implements Runnable {

    ApplicationContext applicationContext;

    AnalysisProperties analysisProperties;

    ObjectFactory<CloneCommand> cloneCommandFactory;
    ObjectFactory<TemporaryDirectory> temporaryDirectoryFactory;
    ObjectFactory<CLOC.Builder> clocCommandFactory;

    GitHubRestConnector gitHubRestConnector;

    ConversionService conversionService;
    GitRepoService gitRepoService;
    LanguageService languageService;

    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${ghs.analysis.delay-between-runs}")
    public void run() {
        log.info(
                "Started analysis on {}/{} repositories",
                gitRepoService.countAnalysisCandidates(),
                gitRepoService.count()
        );
        gitRepoService.streamAnalysisCandidates().forEach(this::analyze);
        Duration delay = analysisProperties.getDelayBetweenRuns();
        Instant instant = Instant.now().plus(delay);
        log.info("Next analysis scheduled for: {}", Date.from(instant));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyze(Pair<Long, String> identifiers) {
        /*
         * Methods annotated with @Async can only be executed outside the caller.
         * Due to this proxying limitation, we have to perform this ugly hack.
         * Why not just make it a service? I feel like the analysis operations
         * are not useful enough on their own to be generalized for other uses.
         */
        applicationContext.getBean(getClass()).gatherCodeMetricsFor(identifiers);
    }

    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param identifiers the ID and full name of the repository.
     */
    @Async("analysisExecutor")
    public void gatherCodeMetricsFor(Pair<Long, String> identifiers) {
        Long id = identifiers.getFirst();
        String name = identifiers.getSecond();
        Optionals.ofThrowable(() -> gitRepoService.getById(id)).ifPresentOrElse(
                this::gatherCodeMetricsFor,
                () -> log.debug("Skipping:  {} [{}]", name, id)
        );
    }

    @SuppressWarnings("DataFlowIssue")
    private void gatherCodeMetricsFor(@NotNull GitRepo gitRepo) {
        Long id = gitRepo.getId();
        String name = gitRepo.getName();
        String uri = String.format("https://github.com/%s.git", name);
        try (
            TemporaryDirectory temporaryDirectory = temporaryDirectoryFactory.getObject();
            Git ignored = cloneCommandFactory.getObject()
                .setURI(uri)
                .setDirectory(temporaryDirectory.file())
                .setDepth(1)
                .call()
        ) {
            log.debug("Analyzing: {} [{}]", name, id);
            Path path = temporaryDirectory.path();
            ObjectNode result = clocCommandFactory.getObject()
                .target(path)
                .linesByLanguage();
            result.remove("header");
            result.remove("SUM");
            Set<GitRepoMetric> metrics = result.properties().stream()
                    .map(entry -> {
                        Language language = languageService.getOrCreate(entry.getKey());
                        GitRepoMetric.Key key = new GitRepoMetric.Key(id, language.getId());
                        GitRepoMetric metric = conversionService.convert(entry.getValue(), GitRepoMetric.class);
                        metric.setKey(key);
                        metric.setRepo(gitRepo);
                        metric.setLanguage(language);
                        return metric;
                    })
                    .collect(Collectors.toSet());
            if (metrics.isEmpty())
                log.info("Skipping:  {} [{}]", name, id);
            gitRepo.setIsLocked(false);
            gitRepo.setIsDisabled(false);
            gitRepo.setMetrics(metrics);
            gitRepo.setLastAnalyzed();
            gitRepoService.createOrUpdate(gitRepo);
        } catch (TransportException ex) {
            HttpStatus status = gitHubRestConnector.pingRepository(name);
            switch (status) {
                case OK -> {
                    String message = ex.getMessage();
                    boolean unauthorized = message.endsWith("not authorized");
                    if (unauthorized) lock(gitRepo);
                    else noop(gitRepo, ex);
                }
                case NOT_FOUND -> delete(gitRepo);
                case FORBIDDEN, UNAVAILABLE_FOR_LEGAL_REASONS -> disable(gitRepo);
                default -> noop(gitRepo, ex);
            }
        } catch (JGitInternalException ex) {
            Throwable cause = ex.getCause();
            noop(gitRepo, cause != null ? cause : ex);
        } catch (IOException | GitAPIException | CLOCException ex) {
            noop(gitRepo, ex);
        }
    }

    private void delete(GitRepo gitRepo) {
        log.info("Deleting:  {} [{}]", gitRepo.getName(), gitRepo.getId());
        gitRepoService.deleteRepoById(gitRepo.getId());
    }

    private void lock(GitRepo gitRepo) {
        log.info("Locking:   {} [{}]", gitRepo.getName(), gitRepo.getId());
        gitRepo.setIsLocked(true);
        gitRepo.setLastAnalyzed();
        gitRepoService.createOrUpdate(gitRepo);
    }

    private void disable(GitRepo gitRepo) {
        log.info("Disabling: {} [{}]", gitRepo.getName(), gitRepo.getId());
        gitRepo.setIsDisabled(true);
        gitRepo.setLastAnalyzed();
        gitRepoService.createOrUpdate(gitRepo);
    }

    private void noop(GitRepo gitRepo, Throwable ex) {
        log.error("Failed:    {} [{}] ({})", gitRepo.getName(), gitRepo.getId(), ex.getClass().getSimpleName());
        log.debug("", ex);
    }
}
