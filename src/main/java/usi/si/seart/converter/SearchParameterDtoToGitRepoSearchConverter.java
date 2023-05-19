package usi.si.seart.converter;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.dto.SearchParameterDto;
import usi.si.seart.repository.specification.GitRepoSearch;
import usi.si.seart.util.Ranges;

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
                .commits(Ranges.build(source.getCommitsMin(), source.getCommitsMax()))
                .contributors(Ranges.build(source.getContributorsMin(), source.getContributorsMax()))
                .issues(Ranges.build(source.getIssuesMin(), source.getIssuesMax()))
                .pulls(Ranges.build(source.getPullsMin(), source.getPullsMax()))
                .branches(Ranges.build(source.getBranchesMin(), source.getBranchesMax()))
                .releases(Ranges.build(source.getReleasesMin(), source.getReleasesMax()))
                .stars(Ranges.build(source.getStarsMin(), source.getStarsMax()))
                .watchers(Ranges.build(source.getWatchersMin(), source.getWatchersMax()))
                .forks(Ranges.build(source.getForksMin(), source.getForksMax()))
                .created(Ranges.build(source.getCreatedMin(), source.getCreatedMax()))
                .committed(Ranges.build(source.getCommittedMin(), source.getCommittedMax()))
                .excludeForks(source.getExcludeForks())
                .onlyForks(source.getOnlyForks())
                .hasIssues(source.getHasIssues())
                .hasPulls(source.getHasPulls())
                .hasWiki(source.getHasWiki())
                .hasLicense(source.getHasLicense())
                .codeLines(Ranges.build(source.getCodeLinesMin(), source.getCodeLinesMax()))
                .commentLines(Ranges.build(source.getCommentLinesMin(), source.getCommentLinesMax()))
                .totalLines(Ranges.build(source.getTotalLinesMin(), source.getTotalLinesMax()))
                .topic(source.getTopic()).build();
    }
}
