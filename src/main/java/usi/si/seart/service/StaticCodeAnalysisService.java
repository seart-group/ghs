package usi.si.seart.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.exception.TerminalExecutionException;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.MetricLanguage;
import usi.si.seart.repository.GitRepoMetricRepository;
import usi.si.seart.repository.GitRepoRepository;
import usi.si.seart.staticcodeanalysis.ClonedRepo;
import usi.si.seart.staticcodeanalysis.TerminalExecution;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service responsible for performing static code analysis on git repositories
 */

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StaticCodeAnalysisService {
    //    ConversionService conversionService;
    GitRepoClonerService gitRepoClonerService;

    GitRepoMetricRepository gitRepoMetricRepository;

    GitRepoRepository gitRepoRepository;


    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param repo_name the name of the git repository (eg "/AlbertCerfeda/example" )
     * @param persist   whether to persist metrics for the given repository.
     *                  If true, a repo with that repo_name needs to exist in the DB.
     *                  If false, the GitRepo object won't be fetched.
     * @return the set of code metrics
     * @throws StaticCodeAnalysisException if an error occurred while performing static code analysis.
     */
    public HashSet<GitRepoMetric> getCodeMetrics(@NotNull String repo_name, boolean persist) throws StaticCodeAnalysisException {
        Gson g = new Gson();
        HashSet<GitRepoMetric> metrics;
        // Deletes the temporary folder of the repo once outside of this clause.
        try (ClonedRepo clonedRepo = gitRepoClonerService.cloneRepo(new URL("https://github.com/" + repo_name)).get()) {
            // Runs cloc for gathering code metrics
            String output = new TerminalExecution(clonedRepo.getPath(), "cloc --vcs=git --json --quiet").start().getStdout().lines().collect(Collectors.joining());
            System.out.println(output);

            // TODO: I think this can lead to side effects
//            metrics = conversionService.convert(Pair.of(repo,g.fromJson(output, JsonObject.class)), HashSet.class);
//            metrics = (new JsonObjectToRepoMetrics()).convert(g.fromJson(output, JsonObject.class)));
            if (!persist) {
                return convert(null, g.fromJson(output, JsonObject.class));
            }

            Optional<GitRepo> repo = gitRepoRepository.findGitRepoByName(repo_name);
            if (repo.isEmpty()) {
                return convert(null, g.fromJson(output, JsonObject.class));
            }

            GitRepo gitrepo = repo.get();
            // Converts the string output from 'cloc' into a set of code metrics.
            metrics = convert(gitrepo, g.fromJson(output, JsonObject.class));
            gitRepoMetricRepository.saveAll(metrics);


        } catch (InterruptedException | ExecutionException | TerminalExecutionException | MalformedURLException e) {
            throw new StaticCodeAnalysisException(e);
        }

        return metrics;
    }

    private HashSet<GitRepoMetric> convert(@Nullable GitRepo repo, @NotNull JsonObject source) {
        HashSet<GitRepoMetric> set = new HashSet<>();
        source.entrySet().stream().filter((Map.Entry<String, JsonElement> entry) ->
                !entry.getKey().equals("header") && !entry.getKey().equals("SUM")
        ).forEach(entry -> {
            JsonObject stat = entry.getValue().getAsJsonObject();
            GitRepoMetric.GitRepoMetricBuilder builder = GitRepoMetric.builder();

            builder.repo(repo);
            builder.language(MetricLanguage.builder().language(entry.getKey()).build());
            builder.blankLines(stat.get("blank").getAsLong());
            builder.commentLines(stat.get("comment").getAsLong());
            builder.codeLines(stat.get("code").getAsLong());

            set.add(builder.build());
        });
        return set;
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void run() throws MalformedURLException, StaticCodeAnalysisException {
//        HashSet<GitRepoMetric> metrics = getCodeMetrics("sparklemotion/nokogiri", true);
//        System.out.println(metrics);
//    }


}



