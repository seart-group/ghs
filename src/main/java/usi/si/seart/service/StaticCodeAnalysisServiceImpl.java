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
import usi.si.seart.repository.GitRepoRepository;
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

    GitRepoClonerService gitRepoClonerService;

    GitRepoRepository gitRepoRepository;


    public Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull GitRepo repo, boolean persist) throws StaticCodeAnalysisException {
        Gson g = new Gson();
        Set<GitRepoMetric> metrics;
        // Deletes the temporary folder of the repo once outside of this clause.
        try (ClonedRepo clonedRepo = gitRepoClonerService.cloneRepo(new URL("https://github.com/" + repo.getName())).get()) {
            // Runs cloc for gathering code metrics
            String output = new TerminalExecution(clonedRepo.getPath(), "cloc --json --quiet .").start().getStdOut().lines().collect(Collectors.joining("\n"));

            // TODO: I think this can lead to side effects
//            metrics = conversionService.convert(Pair.of(repo,g.fromJson(output, JsonObject.class)), HashSet.class);
//            metrics = (new JsonObjectToRepoMetrics()).convert(g.fromJson(output, JsonObject.class)));
            if (!persist) {
                return new AsyncResult<>(convert(null, g.fromJson(output, JsonObject.class)));
            }

            // Converts the string output from 'cloc' into a set of code metrics.
            metrics = convert(repo, g.fromJson(output, JsonObject.class));
            repo.setMetrics(metrics);
            repo.setCloned();
            gitRepoRepository.save(repo);
            log.info("Stored code metrics for repository '{}'", repo.getName());


        } catch (InterruptedException | ExecutionException | TerminalExecutionException | MalformedURLException e) {
            log.error("Could not compute code metrics for repository '{}'", repo.getName(), e);
            throw new StaticCodeAnalysisException(e);
        }

        return new AsyncResult<>(metrics);
    }


    public Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull String repo_name, boolean persist) throws StaticCodeAnalysisException {
        GitRepo repo = gitRepoRepository.findGitRepoByName(repo_name).orElse(null);
        if (repo == null)
            log.warn("Computing metrics for '{}' but did NOT find repo in the database.", repo_name);

        return getCodeMetrics(repo, persist);

    }

    private Set<GitRepoMetric> convert(@Nullable GitRepo repo, @NotNull JsonObject source) {
        return source.entrySet().stream().filter((Map.Entry<String, JsonElement> entry) ->
                !entry.getKey().equals("header") && !entry.getKey().equals("SUM")
        ).map(entry -> {
            JsonObject stat = entry.getValue().getAsJsonObject();
            GitRepoMetric.GitRepoMetricBuilder builder = GitRepoMetric.builder();

            MetricLanguage language = MetricLanguage.builder().language(entry.getKey()).build();
            builder.language(language);
            if (repo != null) {
                builder.repo(repo);
                builder.id(new GitRepoMetricKey(repo.getId(), language.getLanguage()));
            }
            builder.blankLines(stat.get("blank").getAsLong());
            builder.commentLines(stat.get("comment").getAsLong());
            builder.codeLines(stat.get("code").getAsLong());

            return builder.build();
        }).collect(Collectors.toSet());
    }

}