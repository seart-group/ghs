package usi.si.seart.analysis;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.git.GitException;
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
 * Component responsible for mining Git repository code metrics
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StaticCodeAnalyzer {

    ConversionService conversionService;
    GitRepoService gitRepoService;
    LanguageService languageService;

    GitConnector gitConnector;

    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param name the full name of the repository.
     */
    @Async("AnalysisExecutor")
    public void gatherCodeMetricsFor(@NotNull String name) {
        Optionals.ofThrowable(() -> gitRepoService.getByName(name)).ifPresentOrElse(
                this::gatherCodeMetricsFor,
                () -> log.warn("Skipping non-existing repository: [{}]", name)
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
            if (metrics.isEmpty())
                log.warn("No metrics were computed for: {}", name);
            gitRepo.setMetrics(metrics);
            gitRepo.setLastAnalyzed();
            gitRepoService.updateRepo(gitRepo);
        } catch (StaticCodeAnalysisException ex) {
            log.error("Static code analysis failed for: " + name, ex);
        } catch (GitException ex) {
            log.error("Repository cloning has failed, unable to proceed with analysis of: " + name, ex);
        } catch (InterruptedException ex) {
            log.warn("Static code analysis interrupted for: {}", name);
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            log.warn("Static code analysis timed out for: {}", name);
        }
    }
}
