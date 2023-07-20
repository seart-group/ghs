package usi.si.seart.converter;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.collection.Ranges;
import usi.si.seart.dto.SearchParameterDto;
import usi.si.seart.repository.specification.GitRepoSearch;

@AllArgsConstructor
public class SearchParameterDtoToGitRepoSearchConverter implements Converter<SearchParameterDto, GitRepoSearch> {

    @Override
    @NonNull
    public GitRepoSearch convert(@NonNull SearchParameterDto source) {
        return GitRepoSearch.builder()
                .nameEquals(source.getNameEquals())
                .name(source.getName())
                .language(source.getLanguage())
                .license(source.getLicense())
                .label(source.getLabel())
                .commits(Ranges.closed(source.getCommitsMin(), source.getCommitsMax()))
                .contributors(Ranges.closed(source.getContributorsMin(), source.getContributorsMax()))
                .issues(Ranges.closed(source.getIssuesMin(), source.getIssuesMax()))
                .pulls(Ranges.closed(source.getPullsMin(), source.getPullsMax()))
                .branches(Ranges.closed(source.getBranchesMin(), source.getBranchesMax()))
                .releases(Ranges.closed(source.getReleasesMin(), source.getReleasesMax()))
                .stars(Ranges.closed(source.getStarsMin(), source.getStarsMax()))
                .watchers(Ranges.closed(source.getWatchersMin(), source.getWatchersMax()))
                .forks(Ranges.closed(source.getForksMin(), source.getForksMax()))
                .created(Ranges.closed(source.getCreatedMin(), source.getCreatedMax()))
                .committed(Ranges.closed(source.getCommittedMin(), source.getCommittedMax()))
                .excludeForks(source.getExcludeForks())
                .onlyForks(source.getOnlyForks())
                .hasIssues(source.getHasIssues())
                .hasPulls(source.getHasPulls())
                .hasWiki(source.getHasWiki())
                .hasLicense(source.getHasLicense())
                .codeLines(Ranges.closed(source.getCodeLinesMin(), source.getCodeLinesMax()))
                .commentLines(Ranges.closed(source.getCommentLinesMin(), source.getCommentLinesMax()))
                .nonBlankLines(Ranges.closed(source.getNonBlankLinesMin(), source.getNonBlankLinesMax()))
                .topic(source.getTopic()).build();
    }
}
