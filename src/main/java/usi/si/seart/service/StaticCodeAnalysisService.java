package usi.si.seart.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import usi.si.seart.git.GitRepositoryCloner;
import usi.si.seart.git.LocalRepositoryClone;
import usi.si.seart.io.TerminalExecution;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.Language;

import javax.persistence.EntityNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Service responsible for mining code metrics on git repositories
 */
public interface StaticCodeAnalysisService {

    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param name the full name of the repository.
     * @return the set of computed code metrics.
     */
    @Async("GitCloning")
    Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull String name);

    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StaticCodeAnalysisServiceImpl implements StaticCodeAnalysisService {

        Gson gson;

        GitRepoService gitRepoService;
        LanguageService languageService;

        GitRepositoryCloner gitRepositoryCloner;

        @SneakyThrows(MalformedURLException.class)
        public Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull String name) {
            URL url = new URL("https://github.com/" + name);
            try (LocalRepositoryClone localRepository = gitRepositoryCloner.clone(url).get()) {
                GitRepo repo = gitRepoService.getByName(name);
                log.debug("Analyzing repository: {} [{}]", repo.getName(), repo.getId());
                Path path = localRepository.getPath();
                TerminalExecution execution = new TerminalExecution(path, "cloc", "--json", "--quiet", ".");
                TerminalExecution.Result result = execution.execute(5, TimeUnit.MINUTES);
                String output = result.getStdOut();
                Set<GitRepoMetric> metrics = parseCodeMetrics(repo, output);
                repo.setMetrics(metrics);
                repo.setCloned();
                gitRepoService.updateRepo(repo);
                return CompletableFuture.completedFuture(metrics);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                log.warn("Repository cloning has failed, unable to proceed with analysis of: " + name, cause);
                return CompletableFuture.failedFuture(cause);
            } catch (InterruptedException ex) {
                log.warn("Static code analysis interrupted for: {}", name);
                Thread.currentThread().interrupt();
                return CompletableFuture.failedFuture(ex);
            } catch (TimeoutException ex) {
                log.warn("Static code analysis timed out for: {}", name);
                return CompletableFuture.failedFuture(ex);
            } catch (EntityNotFoundException ex) {
                log.error("Static code analysis can not be performed on repository that does not exist: {}", name);
                return CompletableFuture.failedFuture(ex);
            }
        }

        private Set<GitRepoMetric> parseCodeMetrics(@NotNull GitRepo repo, @NotNull String output) {
            return gson.fromJson(output, JsonObject.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals("header") && !entry.getKey().equals("SUM"))
                    .map(entry -> {
                        JsonObject json = entry.getValue().getAsJsonObject();
                        Language language = languageService.getOrCreate(entry.getKey());
                        GitRepoMetric.Key key = new GitRepoMetric.Key(repo.getId(), language.getId());
                        long codeLines = json.get("code").getAsLong();
                        long blankLines = json.get("blank").getAsLong();
                        long commentLines = json.get("comment").getAsLong();
                        return GitRepoMetric.builder()
                                .key(key)
                                .repo(repo)
                                .language(language)
                                .codeLines(codeLines)
                                .blankLines(blankLines)
                                .commentLines(commentLines)
                                .build();
                    })
                    .collect(Collectors.toSet());
        }
    }
}
