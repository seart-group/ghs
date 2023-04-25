package usi.si.seart.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.GitRepoMetricKey;
import usi.si.seart.model.MetricLanguage;
import usi.si.seart.staticcodeanalysis.ClonedRepo;
import usi.si.seart.staticcodeanalysis.TerminalExecution;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StaticCodeAnalysisServiceImpl implements StaticCodeAnalysisService {

    Gson gson;

    GitRepoClonerService gitRepoClonerService;

    GitRepoService gitRepoService;

    MetricLanguageService metricLanguageService;


    public Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull Long repoId, boolean persist) throws StaticCodeAnalysisException {
        GitRepo repo = gitRepoService.getRepoById(repoId).orElseThrow(() -> new StaticCodeAnalysisException("Could not find repo with id " + repoId));

        Set<GitRepoMetric> metrics;
        // Deletes the temporary folder of the repo once outside of this clause.
        try (ClonedRepo clonedRepo = gitRepoClonerService.cloneRepo(new URL("https://github.com/" + repo.getName())).get()) {
            // Runs cloc for gathering code metrics
            String output = new TerminalExecution(clonedRepo.getPath(), "cloc --json --quiet .").start().getStdOut().lines().collect(Collectors.joining("\n"));
            if (!persist) {
                return new AsyncResult<>(parseCodeMetrics(null, output));
            }

            // Converts the string output from 'cloc' into a set of code metrics.
            metrics = parseCodeMetrics(repo, output);
            repo.setMetrics(metrics);
            repo.setCloned();
            gitRepoService.createOrUpdateRepo(repo);
            log.debug("Stored code metrics for repository '{}'", repo.getName());

        } catch (InterruptedException | ExecutionException | TerminalExecutionException | MalformedURLException e) {
            log.error("Could not compute code metrics for repository '{}'", repo.getName(), e);
            throw new StaticCodeAnalysisException(e);
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

            MetricLanguage language = metricLanguageService.getOrCreateMetricLanguage(entry.getKey());
            builder.language(language);
            if (repo != null) {
                builder.repo(repo);
                builder.id(new GitRepoMetricKey(repo.getId(), language.getId()));
            }
            builder.blankLines(languageMetricJson.get("blank").getAsLong());
            builder.commentLines(languageMetricJson.get("comment").getAsLong());
            builder.codeLines(languageMetricJson.get("code").getAsLong());

            return builder.build();
        }).collect(Collectors.toSet());
    }

}