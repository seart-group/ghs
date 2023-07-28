package usi.si.seart.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import usi.si.seart.analysis.ClonedRepo;
import usi.si.seart.analysis.TerminalExecution;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.Language;

import javax.persistence.EntityNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Service responsible for mining code metrics on git repositories
 */
public interface StaticCodeAnalysisService {

    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param repoId the git repo id
     * @param persist whether to persist metrics for the given repository.
     *                If true, a repo with that repo_name needs to exist in the DB.
     *                If false, the GitRepo object won't be fetched.
     * @return the set of code metrics
     * @throws StaticCodeAnalysisException if an error occurred while performing static code analysis.
     */
    @Async("GitCloning")
    Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull Long repoId, boolean persist) throws StaticCodeAnalysisException;

    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class StaticCodeAnalysisServiceImpl implements StaticCodeAnalysisService {

        Gson gson;

        GitRepoClonerService gitRepoClonerService;

        GitRepoService gitRepoService;
        LanguageService languageService;

        @SneakyThrows(MalformedURLException.class)
        public Future<Set<GitRepoMetric>> getCodeMetrics(
                @NotNull Long repoId, boolean persist
        ) throws StaticCodeAnalysisException {
            GitRepo repo;
            try {
                repo = gitRepoService.getRepoById(repoId);
            } catch (EntityNotFoundException ex) {
                throw new StaticCodeAnalysisException("Could not find repo with id " + repoId, ex);
            }

            Set<GitRepoMetric> metrics;
            URL url = new URL("https://github.com/" + repo.getName());
            try (ClonedRepo clonedRepo = gitRepoClonerService.cloneRepo(url).get()) {
                // Runs cloc for gathering code metrics
                String output = new TerminalExecution(clonedRepo.getPath(), "cloc --json --quiet .")
                        .start()
                        .getStdOut()
                        .lines()
                        .collect(Collectors.joining("\n"));
                if (!persist) {
                    return new AsyncResult<>(parseCodeMetrics(null, output));
                }

                // Converts the string output from 'cloc' into a set of code metrics.
                metrics = parseCodeMetrics(repo, output);
                repo.setMetrics(metrics);
                repo.setCloned();
                gitRepoService.updateRepo(repo);
                log.debug("Stored code metrics for repository '{}'", repo.getName());
            } catch (InterruptedException | ExecutionException | TerminalExecutionException ex) {
                log.error("Could not compute code metrics for repository '{}'", repo.getName(), ex);
                throw new StaticCodeAnalysisException(ex);
            }

            return new AsyncResult<>(metrics);
        }

        private Set<GitRepoMetric> parseCodeMetrics(@Nullable GitRepo repo, @NotNull String clocStdout) {
            JsonObject source = gson.fromJson(clocStdout, JsonObject.class);

            return source.entrySet().stream().filter((Map.Entry<String, JsonElement> entry) ->
                    !entry.getKey().equals("header") && !entry.getKey().equals("SUM")
            ).map(entry -> {
                JsonObject languageMetricJson = entry.getValue().getAsJsonObject();
                GitRepoMetric.GitRepoMetricBuilder builder = GitRepoMetric.builder();

                Language language = languageService.getOrCreate(entry.getKey());
                builder.language(language);
                if (repo != null) {
                    builder.repo(repo);
                    builder.key(new GitRepoMetric.Key(repo.getId(), language.getId()));
                }
                builder.blankLines(languageMetricJson.get("blank").getAsLong());
                builder.commentLines(languageMetricJson.get("comment").getAsLong());
                builder.codeLines(languageMetricJson.get("code").getAsLong());

                return builder.build();
            }).collect(Collectors.toSet());
        }
    }
}
