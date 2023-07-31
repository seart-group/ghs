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
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.Language;
import usi.si.seart.service.GitRepoService;
import usi.si.seart.service.LanguageService;

import javax.persistence.EntityNotFoundException;
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
    @SneakyThrows(MalformedURLException.class)
    @SuppressWarnings("ConstantConditions")
    public void gatherCodeMetricsFor(@NotNull String name) {
        URL url = new URL("https://github.com/" + name + ".git");
        try (LocalRepositoryClone localRepository = gitConnector.clone(url)) {
            GitRepo repo = gitRepoService.getByName(name);
            log.debug("Analyzing: {} [{}]", repo.getName(), repo.getId());
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
                        GitRepoMetric.Key key = new GitRepoMetric.Key(repo.getId(), language.getId());
                        JsonObject inner = entry.getValue().getAsJsonObject();
                        GitRepoMetric metric = conversionService.convert(inner, GitRepoMetric.class);
                        metric.setKey(key);
                        metric.setRepo(repo);
                        metric.setLanguage(language);
                        return metric;
                    })
                    .collect(Collectors.toSet());
            if (metrics.isEmpty())
                log.warn("No metrics were computed for: {}", name);
            repo.setMetrics(metrics);
            repo.setCloned();
            gitRepoService.updateRepo(repo);
        } catch (StaticCodeAnalysisException ex) {
            log.error("Static code analysis failed for: " + name, ex);
        } catch (GitException ex) {
            log.error("Repository cloning has failed, unable to proceed with analysis of: " + name, ex);
        } catch (InterruptedException ex) {
            log.warn("Static code analysis interrupted for: {}", name);
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            log.warn("Static code analysis timed out for: {}", name);
        } catch (EntityNotFoundException ex) {
            log.error("Static code analysis can not be performed on repository that does not exist: {}", name);
        }
    }
}
