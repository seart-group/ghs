package ch.usi.si.seart.job;

import ch.usi.si.seart.cloc.CLOCConnector;
import ch.usi.si.seart.config.properties.AnalysisProperties;
import ch.usi.si.seart.exception.StaticCodeAnalysisException;
import ch.usi.si.seart.exception.git.GitException;
import ch.usi.si.seart.exception.git.RepositoryDisabledException;
import ch.usi.si.seart.exception.git.RepositoryLockedException;
import ch.usi.si.seart.exception.git.RepositoryNotFoundException;
import ch.usi.si.seart.exception.git.config.InvalidUsernameException;
import ch.usi.si.seart.git.GitConnector;
import ch.usi.si.seart.git.LocalRepositoryClone;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Language;
import ch.usi.si.seart.model.join.GitRepoMetric;
import ch.usi.si.seart.service.GitRepoService;
import ch.usi.si.seart.service.LanguageService;
import ch.usi.si.seart.stereotype.Job;
import ch.usi.si.seart.util.Optionals;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
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

    GitConnector gitConnector;
    GitHubRestConnector gitHubRestConnector;
    CLOCConnector clocConnector;

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

    @SneakyThrows(MalformedURLException.class)
    @SuppressWarnings("ConstantConditions")
    private void gatherCodeMetricsFor(@NotNull GitRepo gitRepo) {
        Long id = gitRepo.getId();
        String name = gitRepo.getName();
        URL url = new URL("https://github.com/" + name + ".git");
        try (LocalRepositoryClone localRepository = gitConnector.clone(url)) {
            log.debug("Analyzing: {} [{}]", name, id);
            Path path = localRepository.path();
            JsonObject json = clocConnector.analyze(path);
            json.remove("header");
            json.remove("SUM");
            Set<GitRepoMetric> metrics = json.entrySet().stream()
                    .map(entry -> {
                        Language language = languageService.getOrCreate(entry.getKey());
                        GitRepoMetric.Key key = new GitRepoMetric.Key(id, language.getId());
                        JsonObject inner = entry.getValue().getAsJsonObject();
                        GitRepoMetric metric = conversionService.convert(inner, GitRepoMetric.class);
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
        } catch (RepositoryLockedException ignored) {
            log.info("Locking:   {} [{}]", name, id);
            gitRepo.setIsLocked(true);
            gitRepo.setLastAnalyzed();
            gitRepoService.createOrUpdate(gitRepo);
        } catch (RepositoryDisabledException ignored) {
            log.info("Disabling: {} [{}]", name, id);
            gitRepo.setIsDisabled(true);
            gitRepo.setLastAnalyzed();
            gitRepoService.createOrUpdate(gitRepo);
        } catch (RepositoryNotFoundException ignored) {
            log.info("Deleting:  {} [{}]", name, id);
            gitRepoService.deleteRepoById(id);
        } catch (InvalidUsernameException ex) {
            switch (gitHubRestConnector.pingRepository(name)) {
                case NOT_FOUND:
                    log.info("Deleting:  {} [{}]", name, id);
                    gitRepoService.deleteRepoById(id);
                    return;
                case FORBIDDEN, UNAVAILABLE_FOR_LEGAL_REASONS:
                    log.info("Disabling: {} [{}]", name, id);
                    gitRepo.setIsDisabled(true);
                    gitRepo.setLastAnalyzed();
                    gitRepoService.createOrUpdate(gitRepo);
                    return;
                default:
                    log.error("Failed:    {} [{}] ({})", name, id, ex.getClass().getSimpleName());
                    log.debug("", ex);
            }
        } catch (StaticCodeAnalysisException | GitException ex) {
            log.error("Failed:    {} [{}] ({})", name, id, ex.getClass().getSimpleName());
            log.debug("", ex);
        }
    }
}
