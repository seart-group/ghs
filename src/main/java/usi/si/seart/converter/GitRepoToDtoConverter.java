package usi.si.seart.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.dto.GitRepoDto;
import usi.si.seart.dto.GitRepoMetricDTO;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoMetric;
import usi.si.seart.model.Label;
import usi.si.seart.model.Topic;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GitRepoToDtoConverter implements Converter<GitRepo, GitRepoDto> {

    Converter<GitRepoMetric, GitRepoMetricDTO> metricConverter = new GitRepoMetricToDtoConverter();

    @Override
    @NonNull
    public GitRepoDto convert(@NonNull GitRepo source) {
        return GitRepoDto.builder()
                .id(source.getId())
                .name(source.getName())
                .isFork(source.getIsFork())
                .commits(source.getCommits())
                .branches(source.getBranches())
                .defaultBranch(source.getDefaultBranch())
                .releases(source.getReleases())
                .contributors(source.getContributors())
                .license(source.getLicense())
                .watchers(source.getWatchers())
                .stargazers(source.getStargazers())
                .forks(source.getForks())
                .size(source.getSize())
                .createdAt(source.getCreatedAt())
                .pushedAt(source.getPushedAt())
                .updatedAt(source.getUpdatedAt())
                .homepage(source.getHomepage())
                .mainLanguage(source.getMainLanguage())
                .totalIssues(source.getTotalIssues())
                .openIssues(source.getOpenIssues())
                .totalPullRequests(source.getTotalPullRequests())
                .openPullRequests(source.getOpenPullRequests())
                .lastCommit(source.getLastCommit())
                .lastCommitSHA(source.getLastCommitSHA())
                .totalLines(source.getTotalLines())
                .totalCommentLines(source.getTotalCommentLines())
                .totalCodeLines(source.getTotalCodeLines())
                .metrics(source.getMetrics().stream()
                    .map(metricConverter::convert)
                    .collect(Collectors.toList()))
                .hasWiki(source.getHasWiki())
                .isArchived(source.getIsArchived())
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
                .topics(source.getTopics().stream()
                        .map(Topic::getName)
                        .collect(Collectors.toCollection(TreeSet::new))
                )
                .build();
    }
}
