package ch.usi.si.seart.repository.specification;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.GitRepoMetricAggregate_;
import ch.usi.si.seart.model.GitRepo_;
import ch.usi.si.seart.model.Label_;
import ch.usi.si.seart.model.Language_;
import ch.usi.si.seart.model.Topic_;
import ch.usi.si.seart.model.join.GitRepoMetric_;
import ch.usi.si.seart.repository.criteria.AlwaysTrueCriteria;
import ch.usi.si.seart.repository.criteria.Criteria;
import ch.usi.si.seart.repository.criteria.KeyCriteria;
import ch.usi.si.seart.repository.criteria.KeyDualValueCriteria;
import ch.usi.si.seart.repository.criteria.KeyValueCriteria;
import ch.usi.si.seart.repository.operation.BinaryOperation;
import ch.usi.si.seart.repository.operation.TernaryOperation;
import ch.usi.si.seart.repository.operation.UnaryOperation;
import com.google.common.collect.Range;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
public class GitRepoSearch {

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

    boolean hasCodeMetricsFilters() {
        return codeLines.hasLowerBound() || codeLines.hasUpperBound() ||
                commentLines.hasLowerBound() || commentLines.hasUpperBound() ||
                nonBlankLines.hasLowerBound() || nonBlankLines.hasUpperBound();
    }

    public List<Criteria<GitRepo>> toCriteriaList(Root<GitRepo> root) {
        List<Criteria<GitRepo>> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(name)) {
            BinaryOperation operation = (nameEquals) ? BinaryOperation.EQUAL : BinaryOperation.LIKE;
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, operation));
        }

        if (StringUtils.isNotBlank(language)) {
            Path<String> path = root.join(GitRepo_.mainLanguage).get(Language_.name);
            criteria.add(new KeyValueCriteria<>(path, language, BinaryOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(license)) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.license), license, BinaryOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(label)) {
            Path<String> path = root.join(GitRepo_.labels).get(Label_.name);
            criteria.add(new KeyValueCriteria<>(path, label, BinaryOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(topic)) {
            Path<String> path = root.join(GitRepo_.topics).get(Topic_.name);
            criteria.add(new KeyValueCriteria<>(path, topic, BinaryOperation.EQUAL));
        }

        criteria.addAll(List.of(
                toCriteria(root.get(GitRepo_.commits), commits),
                toCriteria(root.get(GitRepo_.contributors), contributors),
                toCriteria(root.get(GitRepo_.totalIssues), issues),
                toCriteria(root.get(GitRepo_.totalPullRequests), pulls),
                toCriteria(root.get(GitRepo_.branches), branches),
                toCriteria(root.get(GitRepo_.releases), releases),
                toCriteria(root.get(GitRepo_.stargazers), stars),
                toCriteria(root.get(GitRepo_.watchers), watchers),
                toCriteria(root.get(GitRepo_.forks), forks),
                toCriteria(root.get(GitRepo_.createdAt), created),
                toCriteria(root.get(GitRepo_.lastCommit), committed)
        ));

        if (hasCodeMetricsFilters()) {
            Path<Long> nonBlankLinesPath;
            Path<Long> codeLinesPath;
            Path<Long> commentLinesPath;

            if (StringUtils.isNotBlank(language)) {
                Path<String> path = root.join(GitRepo_.metrics)
                        .join(GitRepoMetric_.language)
                        .get(Language_.name);
                criteria.add(new KeyValueCriteria<>(path, language, BinaryOperation.EQUAL));
                nonBlankLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.nonBlankLines);
                codeLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.codeLines);
                commentLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.commentLines);
            } else {
                nonBlankLinesPath = root.join(GitRepo_.totalMetrics).get(GitRepoMetricAggregate_.nonBlankLines);
                codeLinesPath = root.join(GitRepo_.totalMetrics).get(GitRepoMetricAggregate_.codeLines);
                commentLinesPath = root.join(GitRepo_.totalMetrics).get(GitRepoMetricAggregate_.commentLines);
            }

            criteria.add(toCriteria(nonBlankLinesPath, nonBlankLines));
            criteria.add(toCriteria(codeLinesPath, codeLines));
            criteria.add(toCriteria(commentLinesPath, commentLines));
        }

        if (excludeForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), false, BinaryOperation.EQUAL));
        } else if (onlyForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), true, BinaryOperation.EQUAL));
        }

        if (hasIssues) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openIssues), 0L, BinaryOperation.GREATER_THAN));
        }

        if (hasPulls) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openPullRequests), 0L, BinaryOperation.GREATER_THAN));
        }

        if (hasWiki) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.hasWiki), true, BinaryOperation.EQUAL));
        }

        if (hasLicense) {
            criteria.add(new KeyCriteria<>(root.get(GitRepo_.license), UnaryOperation.IS_NOT_NULL));
        }

        return criteria;
    }

    private static <T extends Comparable<T>> Criteria<GitRepo> toCriteria(Path<T> key, Range<T> range) {
        boolean lowerBound = range.hasLowerBound();
        boolean upperBound = range.hasUpperBound();
        if (lowerBound && upperBound) {
            T lower = range.lowerEndpoint();
            T upper = range.upperEndpoint();
            return new KeyDualValueCriteria<>(key, lower, upper, TernaryOperation.BETWEEN);
        } else if (lowerBound) {
            T lower = range.lowerEndpoint();
            return new KeyValueCriteria<>(key, lower, BinaryOperation.GREATER_THAN_EQUAL);
        } else if (upperBound) {
            T upper = range.upperEndpoint();
            return new KeyValueCriteria<>(key, upper, BinaryOperation.LESS_THAN_EQUAL);
        } else {
            return new AlwaysTrueCriteria<>();
        }
    }
}
