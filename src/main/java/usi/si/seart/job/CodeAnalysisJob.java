package usi.si.seart.job;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.git.GitException;
import usi.si.seart.exception.git.RepositoryNotFoundException;
import usi.si.seart.git.GitConnector;
import usi.si.seart.git.LocalRepositoryClone;
import usi.si.seart.io.ExternalProcess;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.Language;
import usi.si.seart.model.join.GitRepoMetric;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.LanguageService;
import usi.si.seart.util.Optionals;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Mines code metrics for every needed repository.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.crawl.analysis.enabled", havingValue = "true")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeAnalysisJob {

    ApplicationContext applicationContext;

    GitConnector gitConnector;

    ConversionService conversionService;
    GitRepoService gitRepoService;
    LanguageService languageService;

    @Transactional(readOnly = true)
    @Scheduled(fixedDelayString = "${app.crawl.analysis.scheduling}")
    public void run() {
        log.info(
                "Started analysis on {}/{} repositories",
                gitRepoService.countAnalysisCandidates(),
                gitRepoService.count()
        );
        gitRepoService.streamAnalysisCandidates().forEach(this::analyze);
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
    @Async("AnalysisExecutor")
    public void gatherCodeMetricsFor(Pair<Long, String> identifiers) {
        Long id = identifiers.getFirst();
        String name = identifiers.getSecond();
        Optionals.ofThrowable(() -> gitRepoService.getById(id))
                .ifPresentOrElse(
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
            Path path = localRepository.getPath();
            ExternalProcess process = new ExternalProcess(path, "cloc", "--json", "--quiet", ".");
            ExternalProcess.Result result = process.execute(5, TimeUnit.MINUTES);
            if (!result.succeeded())
                throw new StaticCodeAnalysisException(result.getStdErr());
            String output = result.getStdOut();
            JsonObject json = conversionService.convert(output, JsonObject.class);
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
            if (metrics.isEmpty()) {
                log.debug("No metrics were computed for: {}", name);
                log.info("Skipping:  {} [{}]", name, id);
            }
            gitRepo.setMetrics(metrics);
            gitRepo.setLastAnalyzed();
            gitRepoService.createOrUpdate(gitRepo);
        } catch (RepositoryNotFoundException ignored) {
            log.debug("Remote not found {}, performing cleanup instead...", name);
            log.info("Deleting:  {} [{}]", name, id);
            gitRepoService.deleteRepoById(id);
        } catch (InterruptedException ex) {
            log.warn("Interrupt: {} [{}]", name, id);
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            log.warn("Timeout:   {} [{}]", name, id);
        } catch (StaticCodeAnalysisException | GitException ex) {
            log.error("Failed:    {} [{}] ({})", name, id, ex.getClass().getSimpleName());
            log.debug("", ex);
        }
    }
}
