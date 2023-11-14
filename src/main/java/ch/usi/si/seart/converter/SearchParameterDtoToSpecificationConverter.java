package ch.usi.si.seart.converter;

import ch.usi.si.seart.collection.Ranges;
import ch.usi.si.seart.dto.SearchParameterDto;
import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.GitRepoMetricAggregate;
import ch.usi.si.seart.model.GitRepoMetricAggregate_;
import ch.usi.si.seart.model.GitRepo_;
import ch.usi.si.seart.model.Label_;
import ch.usi.si.seart.model.Language_;
import ch.usi.si.seart.model.Topic_;
import ch.usi.si.seart.model.join.GitRepoMetric_;
import ch.usi.si.seart.repository.criteria.AlwaysTrueCriteria;
import ch.usi.si.seart.repository.criteria.Criteria;
import ch.usi.si.seart.repository.criteria.CriteriaConjunction;
import ch.usi.si.seart.repository.criteria.KeyCriteria;
import ch.usi.si.seart.repository.criteria.KeyValueCriteria;
import ch.usi.si.seart.repository.operation.BinaryOperation;
import ch.usi.si.seart.repository.operation.UnaryOperation;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class SearchParameterDtoToSpecificationConverter
        implements Converter<SearchParameterDto, Specification<GitRepo>>
{

    @Override
    @NotNull
    public Specification<GitRepo> convert(@NotNull SearchParameterDto source) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Adapter adapter = new Adapter(root, source);
            Predicate[] predicates = adapter.getCriteria().stream()
                    .filter(java.util.function.Predicate.not(AlwaysTrueCriteria.class::isInstance))
                    .map(criteria -> criteria.toPredicate(root, criteriaQuery, criteriaBuilder))
                    .toArray(Predicate[]::new);
            criteriaQuery.distinct(true);
            return criteriaBuilder.and(predicates);
        };
    }
    
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class Adapter {

        Root<GitRepo> root;

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

        Adapter(Root<GitRepo> root, SearchParameterDto dto) {
            this.root = root;

            this.name = dto.getName();
            this.nameEquals = dto.getNameEquals();
            
            this.language = dto.getLanguage();
            this.license = dto.getLicense();
            this.label = dto.getLabel();
            this.topic = dto.getTopic();
            
            this.commits = dto.getCommits();
            this.contributors = dto.getContributors();
            this.issues = dto.getIssues();
            this.pulls = dto.getPulls();
            this.branches = dto.getBranches();
            this.releases = dto.getReleases();
            this.stars = dto.getStars();
            this.watchers = dto.getWatchers();
            this.forks = dto.getForks();

            this.codeLines = dto.getCodeLines();
            this.commentLines = dto.getCommentLines();
            this.nonBlankLines = dto.getNonBlankLines();
            
            this.created = dto.getCreated();
            this.committed = dto.getCommitted();

            this.excludeForks = dto.getExcludeForks();
            this.onlyForks = dto.getOnlyForks();
            this.hasIssues = dto.getHasIssues();
            this.hasPulls = dto.getHasPulls();
            this.hasWiki = dto.getHasWiki();
            this.hasLicense = dto.getHasLicense();
        }

        boolean hasCodeMetricsFilters() {
            return Stream.of(codeLines, commentLines, nonBlankLines).anyMatch(Ranges::hasAnyBound);
        }

        public List<Criteria<GitRepo>> getCriteria() {
            List<Criteria<GitRepo>> criteria = List.of(
                    getNameCriteria(),
                    getLanguageCriteria(),
                    getLicenseCriteria(),
                    getLabelCriteria(),
                    getTopicCriteria(),
                    getCommitsCriteria(),
                    getContributorsCriteria(),
                    getIssuesCriteria(),
                    getPullsCriteria(),
                    getBranchesCriteria(),
                    getReleasesCriteria(),
                    getStarsCriteria(),
                    getWatchersCriteria(),
                    getForksCriteria(),
                    getCreatedCriteria(),
                    getLastCommitCriteria(),
                    getCodeMetricsCriteria(),
                    getIsForkCriteria(),
                    getHasIssuesCriteria(),
                    getHasPullsCriteria(),
                    getHasWiki(),
                    getHasLicense()
            );
            return criteria.stream().filter(criterion -> !(criterion instanceof AlwaysTrueCriteria<GitRepo>)).toList();
        }

        private Criteria<GitRepo> getNameCriteria() {
            if (!StringUtils.hasText(name)) return new AlwaysTrueCriteria<>();
            BinaryOperation operation = (nameEquals) ? BinaryOperation.EQUAL : BinaryOperation.LIKE;
            return new KeyValueCriteria<>(root.get(GitRepo_.name), name, operation);
        }

        private Criteria<GitRepo> getLanguageCriteria() {
            if (!StringUtils.hasText(language)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.mainLanguage).get(Language_.name);
            return new KeyValueCriteria<>(path, language, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getLicenseCriteria() {
            if (!StringUtils.hasText(license)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.get(GitRepo_.license);
            return new KeyValueCriteria<>(path, license, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getLabelCriteria() {
            if (!StringUtils.hasText(label)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.labels).get(Label_.name);
            return new KeyValueCriteria<>(path, label, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getTopicCriteria() {
            if (!StringUtils.hasText(topic)) return new AlwaysTrueCriteria<>();
            Path<String> path = root.join(GitRepo_.topics).get(Topic_.name);
            return new KeyValueCriteria<>(path, topic, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getCommitsCriteria() {
            return Criteria.forRange(root.get(GitRepo_.commits), commits);
        }

        private Criteria<GitRepo> getContributorsCriteria() {
            return Criteria.forRange(root.get(GitRepo_.contributors), contributors);
        }

        private Criteria<GitRepo> getIssuesCriteria() {
            Path<Long> path = root.get(GitRepo_.totalIssues);
            return Criteria.forRange(path, issues);
        }

        private Criteria<GitRepo> getPullsCriteria() {
            Path<Long> path = root.get(GitRepo_.totalPullRequests);
            return Criteria.forRange(path, pulls);
        }

        private Criteria<GitRepo> getBranchesCriteria() {
            Path<Long> path = root.get(GitRepo_.branches);
            return Criteria.forRange(path, branches);
        }

        private Criteria<GitRepo> getReleasesCriteria() {
            Path<Long> path = root.get(GitRepo_.releases);
            return Criteria.forRange(path, releases);
        }

        private Criteria<GitRepo> getStarsCriteria() {
            Path<Long> path = root.get(GitRepo_.stargazers);
            return Criteria.forRange(path, stars);
        }

        private Criteria<GitRepo> getWatchersCriteria() {
            Path<Long> path = root.get(GitRepo_.watchers);
            return Criteria.forRange(path, watchers);
        }

        private Criteria<GitRepo> getForksCriteria() {
            Path<Long> path = root.get(GitRepo_.forks);
            return Criteria.forRange(path, forks);
        }

        private Criteria<GitRepo> getCreatedCriteria() {
            Path<Date> path = root.get(GitRepo_.createdAt);
            return Criteria.forRange(path, created);
        }

        private Criteria<GitRepo> getLastCommitCriteria() {
            Path<Date> path = root.get(GitRepo_.lastCommit);
            return Criteria.forRange(path, committed);
        }

        private Criteria<GitRepo> getCodeMetricsCriteria() {
            if (!hasCodeMetricsFilters()) return new AlwaysTrueCriteria<>();
            if (!StringUtils.hasText(language)) {
                SingularAttribute<GitRepo, GitRepoMetricAggregate> attribute = GitRepo_.totalMetrics;
                Path<Long> nonBlankLinesPath = root.join(attribute).get(GitRepoMetricAggregate_.nonBlankLines);
                Path<Long> codeLinesPath = root.join(attribute).get(GitRepoMetricAggregate_.codeLines);
                Path<Long> commentLinesPath = root.join(attribute).get(GitRepoMetricAggregate_.commentLines);
                return new CriteriaConjunction<>(
                        Criteria.forRange(nonBlankLinesPath, nonBlankLines),
                        Criteria.forRange(codeLinesPath, codeLines),
                        Criteria.forRange(commentLinesPath, commentLines)
                );
            } else {
                Path<String> languagePath = root.join(GitRepo_.metrics)
                        .join(GitRepoMetric_.language)
                        .get(Language_.name);
                Path<Long> nonBlankLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.nonBlankLines);
                Path<Long> codeLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.codeLines);
                Path<Long> commentLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.commentLines);
                return new CriteriaConjunction<>(
                        new KeyValueCriteria<>(languagePath, language, BinaryOperation.EQUAL),
                        Criteria.forRange(nonBlankLinesPath, nonBlankLines),
                        Criteria.forRange(codeLinesPath, codeLines),
                        Criteria.forRange(commentLinesPath, commentLines)
                );
            }
        }

        private Criteria<GitRepo> getIsForkCriteria() {
            if (excludeForks) {
                return new KeyValueCriteria<>(root.get(GitRepo_.isFork), false, BinaryOperation.EQUAL);
            } else if (onlyForks) {
                return new KeyValueCriteria<>(root.get(GitRepo_.isFork), true, BinaryOperation.EQUAL);
            } else {
                return new AlwaysTrueCriteria<>();
            }
        }

        private Criteria<GitRepo> getHasIssuesCriteria() {
            if (!hasIssues) return new AlwaysTrueCriteria<>();
            Path<Long> path = root.get(GitRepo_.openIssues);
            return new KeyValueCriteria<>(path, 0L, BinaryOperation.GREATER_THAN);
        }

        private Criteria<GitRepo> getHasPullsCriteria() {
            if (!hasPulls) return new AlwaysTrueCriteria<>();
            Path<Long> path = root.get(GitRepo_.openPullRequests);
            return new KeyValueCriteria<>(path, 0L, BinaryOperation.GREATER_THAN);
        }

        private Criteria<GitRepo> getHasWiki() {
            if (!hasWiki) return new AlwaysTrueCriteria<>();
            Path<Boolean> path = root.get(GitRepo_.hasWiki);
            return new KeyValueCriteria<>(path, true, BinaryOperation.EQUAL);
        }

        private Criteria<GitRepo> getHasLicense() {
            if (!hasLicense) return new AlwaysTrueCriteria<>();
            Path<String> path = root.get(GitRepo_.license);
            return new KeyCriteria<>(path, UnaryOperation.IS_NOT_NULL);
        }
    }
}
