package ch.usi.si.seart.converter;

import ch.usi.si.seart.dto.GitRepoCsvDto;
import ch.usi.si.seart.dto.GitRepoDto;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Convert a GitRepoDTO to it's CSV representation.
 */
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoDtoToCsvConverter implements Converter<GitRepoDto, GitRepoCsvDto> {

    CsvMapper csvMapper;

    @Override
    @NonNull
    public GitRepoCsvDto convert(@NonNull GitRepoDto source) {
        return GitRepoCsvDto.builder()
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
                .blankLines(source.getBlankLines())
                .codeLines(source.getCodeLines())
                .commentLines(source.getCommentLines())
                .metrics(source.getMetrics())
                .lastCommit(source.getLastCommit())
                .lastCommitSHA(source.getLastCommitSHA())
                // Convert Collection types into stringified JSON
                .metricsString(csvMapper.valueToTree(source.getMetrics()).toString())
                .labelsString(csvMapper.valueToTree(source.getLabels()).toString())
                .languagesString(csvMapper.valueToTree(source.getLanguages()).toString())
                .topicsString(csvMapper.valueToTree(source.getTopics()).toString())
                .build();
    }
}
