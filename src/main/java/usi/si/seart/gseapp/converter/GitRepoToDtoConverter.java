package usi.si.seart.gseapp.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GitRepoToDtoConverter implements Converter<GitRepo, GitRepoDto> {

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
                .hasWiki(source.getHasWiki())
                .isArchived(source.getIsArchived())
                .languages(
                        source.getLanguages().stream()
                                .map(l -> Map.entry(l.getLanguage(), l.getSizeOfCode()))
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
                                .map(GitRepoLabel::getLabel)
                                .collect(Collectors.toCollection(TreeSet::new))
                )
                .build();
    }
}
