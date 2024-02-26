package ch.usi.si.seart.converter;

import ch.usi.si.seart.dto.SearchParameterDto;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.GitRepo_;
import ch.usi.si.seart.model.Label_;
import ch.usi.si.seart.model.Language_;
import ch.usi.si.seart.model.License_;
import ch.usi.si.seart.model.Topic_;
import ch.usi.si.seart.model.join.GitRepoMetric;
import ch.usi.si.seart.model.join.GitRepoMetricAggregate;
import ch.usi.si.seart.model.join.GitRepoMetricAggregate_;
import ch.usi.si.seart.model.join.GitRepoMetric_;
import ch.usi.si.seart.repository.criteria.AlwaysTrueCriteria;
import ch.usi.si.seart.repository.criteria.Criteria;
import ch.usi.si.seart.repository.criteria.CriteriaConjunction;
import ch.usi.si.seart.repository.criteria.KeyCriteria;
import ch.usi.si.seart.repository.criteria.KeyValueCriteria;
import ch.usi.si.seart.repository.operation.BinaryOperation;
import ch.usi.si.seart.repository.operation.UnaryOperation;
import ch.usi.si.seart.util.Ranges;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Date;
import java.util.stream.Stream;

public class SearchParameterDtoToSpecificationConverter
        implements Converter<SearchParameterDto, Specification<GitRepo>>
{

    @Override
    @NotNull
    public Specification<GitRepo> convert(@NotNull SearchParameterDto source) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            SearchCriteria criteria = new SearchCriteria(source);
            Predicate predicate = criteria.toPredicate(root, criteriaQuery, criteriaBuilder);
            criteriaQuery.distinct(true);
            return criteriaBuilder.and(predicate);
        };
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class SearchCriteria implements Criteria<GitRepo> {

        String name;
        boolean nameEquals;

        String language;
        String license;
        String label;
        String topic;

        Range<Long> commits;
        Range<Long> contributors;
        Range<Long> issues;
        Range<Long> pulls;
        Range<Long> branches;
        Range<Long> releases;
        Range<Long> stars;
        Range<Long> watchers;
        Range<Long> forks;
        
        Range<Date> created;
        Range<Date> committed;
        
        Range<Long> codeLines;
        Range<Long> commentLines;
        Range<Long> nonBlankLines;

        boolean excludeForks;
        boolean onlyForks;
        boolean hasIssues;
        boolean hasPulls;
        boolean hasWiki;
        boolean hasLicense;

        SearchCriteria(SearchParameterDto dto) {
            this(
                    dto.getName(),
                    dto.getNameEquals(),
                    dto.getLanguage(),
                    dto.getLicense(),
                    dto.getLabel(),
                    dto.getTopic(),
                    dto.getCommits(),
                    dto.getContributors(),
                    dto.getIssues(),
                    dto.getPulls(),
                    dto.getBranches(),
                    dto.getReleases(),
                    dto.getStars(),
                    dto.getWatchers(),
                    dto.getForks(),
                    dto.getCreated(),
                    dto.getCommitted(),
                    dto.getCodeLines(),
                    dto.getCommentLines(),
                    dto.getNonBlankLines(),
                    dto.getExcludeForks(),
                    dto.getOnlyForks(),
                    dto.getHasIssues(),
                    dto.getHasPulls(),
                    dto.getHasWiki(),
                    dto.getHasLicense()
            );
        }

        @Override
        public Predicate toPredicate(
                @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
        ) {
            return getCriteria(root).toPredicate(root, query, criteriaBuilder);
        }

        private Criteria<GitRepo> getCriteria(@NotNull Root<GitRepo> root) {
            return new CriteriaConjunction<>(
                    getNameCriteria(root),
                    getLanguageCriteria(root),
                    getLicenseCriteria(root),
                    getLabelCriteria(root),
                    getTopicCriteria(root),
                    getCommitsCriteria(root),
                    getContributorsCriteria(root),
                    getIssuesCriteria(root),
                    getPullsCriteria(root),
                    getBranchesCriteria(root),
                    getReleasesCriteria(root),
                    getStarsCriteria(root),
                    getWatchersCriteria(root),
                    getForksCriteria(root),
                    getCreatedCriteria(root),
                    getLastCommitCriteria(root),
                    getCodeMetricsCriteria(root),
                    getIsForkCriteria(root),
                    getHasIssuesCriteria(root),
                    getHasPullsCriteria(root),
                    getHasWikiCriteria(root),
                    getHasLicenseCriteria(root)
            );
        }

        private Criteria<GitRepo> getNameCriteria(@NotNull Root<GitRepo> root) {
            if (!StringUtils.hasText(name)) return new AlwaysTrueCriteria<>();
            BinaryOperation operation = (nameEquals) ? BinaryOperation.EQUAL : BinaryOperation.LIKE;
            return new KeyValueCriteria<>(root.get(GitRepo_.name), name, operation);
        }

        private Criteria<GitRepo> getLanguageCriteria(@NotNull Root<GitRepo> root) {
            if (!StringUtils.hasText(language)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.mainLanguage).get(Language_.name);
            return new KeyValueCriteria<>(path, language, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getLicenseCriteria(@NotNull Root<GitRepo> root) {
            if (!StringUtils.hasText(license)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.license).get(License_.name);
            return new CriteriaConjunction<>(
                    new KeyCriteria<>(path, UnaryOperation.IS_NOT_NULL),
                    new KeyValueCriteria<>(path, license, BinaryOperation.EQUAL)
            );
        }

        private Criteria<GitRepo> getLabelCriteria(@NotNull Root<GitRepo> root) {
            if (!StringUtils.hasText(label)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.labels).get(Label_.name);
            return new KeyValueCriteria<>(path, label, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getTopicCriteria(@NotNull Root<GitRepo> root) {
            if (!StringUtils.hasText(topic)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.topics).get(Topic_.name);
            return new KeyValueCriteria<>(path, topic, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getCommitsCriteria(@NotNull Root<GitRepo> root) {
            return Criteria.forRange(root.get(GitRepo_.commits), commits);
        }

        private Criteria<GitRepo> getContributorsCriteria(@NotNull Root<GitRepo> root) {
            return Criteria.forRange(root.get(GitRepo_.contributors), contributors);
        }

        private Criteria<GitRepo> getIssuesCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.totalIssues);
            return Criteria.forRange(path, issues);
        }

        private Criteria<GitRepo> getPullsCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.totalPullRequests);
            return Criteria.forRange(path, pulls);
        }

        private Criteria<GitRepo> getBranchesCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.branches);
            return Criteria.forRange(path, branches);
        }

        private Criteria<GitRepo> getReleasesCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.releases);
            return Criteria.forRange(path, releases);
        }

        private Criteria<GitRepo> getStarsCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.stargazers);
            return Criteria.forRange(path, stars);
        }

        private Criteria<GitRepo> getWatchersCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.watchers);
            return Criteria.forRange(path, watchers);
        }

        private Criteria<GitRepo> getForksCriteria(@NotNull Root<GitRepo> root) {
            Path<Long> path = root.get(GitRepo_.forks);
            return Criteria.forRange(path, forks);
        }

        private Criteria<GitRepo> getCreatedCriteria(@NotNull Root<GitRepo> root) {
            Path<Date> path = root.get(GitRepo_.createdAt);
            return Criteria.forRange(path, created);
        }

        private Criteria<GitRepo> getLastCommitCriteria(@NotNull Root<GitRepo> root) {
            Path<Date> path = root.get(GitRepo_.lastCommit);
            return Criteria.forRange(path, committed);
        }

        private boolean hasCodeMetricsFilters() {
            return Stream.of(codeLines, commentLines, nonBlankLines).anyMatch(Ranges::hasAnyBound);
        }

        private Criteria<GitRepo> getCodeMetricsCriteria(@NotNull Root<GitRepo> root) {
            if (!hasCodeMetricsFilters()) return new AlwaysTrueCriteria<>();
            if (!StringUtils.hasText(language)) {
                Join<GitRepo, GitRepoMetricAggregate> join = root.join(GitRepo_.totalMetrics);
                Path<Long> nonBlankLinesPath = join.get(GitRepoMetricAggregate_.nonBlankLines);
                Path<Long> codeLinesPath = join.get(GitRepoMetricAggregate_.codeLines);
                Path<Long> commentLinesPath = join.get(GitRepoMetricAggregate_.commentLines);
                return new CriteriaConjunction<>(
                        Criteria.forRange(nonBlankLinesPath, nonBlankLines),
                        Criteria.forRange(codeLinesPath, codeLines),
                        Criteria.forRange(commentLinesPath, commentLines)
                );
            } else {
                Join<GitRepo, GitRepoMetric> join = root.join(GitRepo_.metrics);
                Path<Long> nonBlankLinesPath = join.get(GitRepoMetric_.nonBlankLines);
                Path<Long> codeLinesPath = join.get(GitRepoMetric_.codeLines);
                Path<Long> commentLinesPath = join.get(GitRepoMetric_.commentLines);
                Path<String> languagePath = join.join(GitRepoMetric_.language).get(Language_.name);
                return new CriteriaConjunction<>(
                        new KeyValueCriteria<>(languagePath, language, BinaryOperation.EQUAL),
                        Criteria.forRange(nonBlankLinesPath, nonBlankLines),
                        Criteria.forRange(codeLinesPath, codeLines),
                        Criteria.forRange(commentLinesPath, commentLines)
                );
            }
        }

        private Criteria<GitRepo> getIsForkCriteria(@NotNull Root<GitRepo> root) {
            if (!excludeForks && !onlyForks) return new AlwaysTrueCriteria<>();
            Path<Boolean> path = root.get(GitRepo_.isFork);
            UnaryOperation operation = (excludeForks) ? UnaryOperation.IS_FALSE : UnaryOperation.IS_TRUE;
            return new KeyCriteria<>(path, operation);
        }

        private Criteria<GitRepo> getHasIssuesCriteria(@NotNull Root<GitRepo> root) {
            if (!hasIssues) return new AlwaysTrueCriteria<>();
            Path<Long> path = root.get(GitRepo_.openIssues);
            return new KeyValueCriteria<>(path, 0L, BinaryOperation.GREATER_THAN);
        }

        private Criteria<GitRepo> getHasPullsCriteria(@NotNull Root<GitRepo> root) {
            if (!hasPulls) return new AlwaysTrueCriteria<>();
            Path<Long> path = root.get(GitRepo_.openPullRequests);
            return new KeyValueCriteria<>(path, 0L, BinaryOperation.GREATER_THAN);
        }

        private Criteria<GitRepo> getHasWikiCriteria(@NotNull Root<GitRepo> root) {
            if (!hasWiki) return new AlwaysTrueCriteria<>();
            Path<Boolean> path = root.get(GitRepo_.hasWiki);
            return new KeyCriteria<>(path, UnaryOperation.IS_TRUE);
        }

        private Criteria<GitRepo> getHasLicenseCriteria(@NotNull Root<GitRepo> root) {
            if (StringUtils.hasText(license) || !hasLicense) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.license).get(License_.name);
            return new KeyCriteria<>(path, UnaryOperation.IS_NOT_NULL);
        }
    }
}
