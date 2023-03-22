package usi.si.seart.service;

import org.springframework.scheduling.annotation.Async;
import usi.si.seart.exception.StaticCodeAnalysisException;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Service responsible for performing static code analysis on git repositories
 */
public interface StaticCodeAnalysisService {

    /**
     * Computes the set of code metrics of a given repository.
     *
     * @param repo    the git repo
     * @param persist whether to persist metrics for the given repository.
     *                If true, a repo with that repo_name needs to exist in the DB.
     *                If false, the GitRepo object won't be fetched.
     * @return the set of code metrics
     * @throws StaticCodeAnalysisException if an error occurred while performing static code analysis.
     */
    @Async("GitCloning")
    Future<Set<GitRepoMetric>> getCodeMetrics(@NotNull GitRepo repo, boolean persist) throws StaticCodeAnalysisException;

}