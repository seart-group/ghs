package ch.usi.si.seart.converter;

import ch.usi.si.seart.dto.GitRepoDto;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Label;
import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.model.join.GitRepoMetricAggregate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GitRepoToDtoConverter implements Converter<GitRepo, GitRepoDto> {

    @Override
    @NonNull
    public GitRepoDto convert(@NonNull GitRepo source) {
        GitRepoMetricAggregate totalMetrics = source.getTotalMetrics();
        boolean hasMetrics = totalMetrics != null;

        return GitRepoDto.builder()
                .id(source.getId())
                .name(source.getName())
                .isFork(source.getIsFork())
                .commits(source.getCommits())
                .branches(source.getBranches())
                .defaultBranch(source.getDefaultBranch())
                .releases(source.getReleases())
                .contributors(source.getContributors())
                .license(source.getLicense().getName())
                .watchers(source.getWatchers())
                .stargazers(source.getStargazers())
                .forks(source.getForks())
                .size(source.getSize())
                .createdAt(source.getCreatedAt())
                .pushedAt(source.getPushedAt())
                .updatedAt(source.getUpdatedAt())
                .homepage(source.getHomepage())
                .mainLanguage(source.getMainLanguage().getName())
                .totalIssues(source.getTotalIssues())
                .openIssues(source.getOpenIssues())
                .totalPullRequests(source.getTotalPullRequests())
                .openPullRequests(source.getOpenPullRequests())
                .lastCommit(source.getLastCommit())
                .lastCommitSHA(source.getLastCommitSHA())
                .blankLines(hasMetrics ? totalMetrics.getBlankLines() : null)
                .commentLines(hasMetrics ? totalMetrics.getCommentLines() : null)
                .codeLines(hasMetrics ? totalMetrics.getCodeLines() : null)
                .metrics(
                        source.getMetrics().stream()
                                .map(metric -> Map.<String, Object>of(
                                        "language", metric.getLanguage().getName(),
                                        "codeLines", metric.getCodeLines(),
                                        "blankLines", metric.getBlankLines(),
                                        "commentLines", metric.getCommentLines()
                                ))
                                .toList()
                )
                .hasWiki(source.getHasWiki())
                .isArchived(source.getIsArchived())
                .isDisabled(source.getIsDisabled())
                .isLocked(source.getIsLocked())
                .languages(
                        source.getLanguages().stream()
                                .map(l -> Map.entry(l.getLanguage().getName(), l.getSizeOfCode()))
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (x, y) -> y,
                                        LinkedHashMap::new
                                ))
                )
                .labels(
                        source.getLabels().stream()
                                .map(Label::getName)
                                .collect(Collectors.toCollection(TreeSet::new))
                )
                .topics(
                        source.getTopics().stream()
                                .map(Topic::getName)
                                .collect(Collectors.toCollection(TreeSet::new))
                )
                .build();
    }
}
