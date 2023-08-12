package ch.usi.si.seart.repository.specification;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.GitRepoMetricAggregate_;
import ch.usi.si.seart.model.GitRepo_;
import ch.usi.si.seart.model.Label_;
import ch.usi.si.seart.model.Language_;
import ch.usi.si.seart.model.Topic_;
import ch.usi.si.seart.model.join.GitRepoMetric_;
import ch.usi.si.seart.repository.criteria.Criteria;
import ch.usi.si.seart.repository.criteria.KeyCriteria;
import ch.usi.si.seart.repository.criteria.KeyValueCriteria;
import ch.usi.si.seart.repository.operation.BinaryOperation;
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
            if (nameEquals) {
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.EQUAL));
            } else {
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.LIKE));
            }
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

        if (commits.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.commits),
                            commits.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (commits.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.commits),
                            commits.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (contributors.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.contributors),
                            contributors.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (contributors.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.contributors),
                            contributors.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (issues.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.totalIssues),
                            issues.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (issues.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.totalIssues),
                            issues.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (pulls.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.totalPullRequests),
                            pulls.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (pulls.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.totalPullRequests),
                            pulls.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (branches.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.branches),
                            branches.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (branches.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.branches),
                            branches.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (releases.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.releases),
                            releases.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (releases.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.releases),
                            releases.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (stars.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.stargazers),
                            stars.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (stars.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.stargazers),
                            stars.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (watchers.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.watchers),
                            watchers.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (watchers.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.watchers),
                            watchers.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (forks.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.forks),
                            forks.lowerEndpoint(),
                            BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (forks.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.forks),
                            forks.upperEndpoint(),
                            BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }


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

            if (nonBlankLines.hasLowerBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                nonBlankLinesPath, nonBlankLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL
                        )
                );
            }
            if (nonBlankLines.hasUpperBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                nonBlankLinesPath, nonBlankLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL
                        )
                );
            }

            if (codeLines.hasLowerBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                codeLinesPath, codeLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL
                        )
                );
            }
            if (codeLines.hasUpperBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                codeLinesPath, codeLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL
                        )
                );
            }

            if (commentLines.hasLowerBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                commentLinesPath, commentLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL
                        )
                );
            }
            if (commentLines.hasUpperBound()) {
                criteria.add(
                        new KeyValueCriteria<>(
                                commentLinesPath, commentLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL
                        )
                );
            }
        }

        if (StringUtils.isNotBlank(topic)) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.join(GitRepo_.topics).get(Topic_.name),
                            topic,
                            BinaryOperation.EQUAL
                    )
            );
        }

        if (created.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.createdAt), created.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (created.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.createdAt), created.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL
                    )
            );
        }

        if (committed.hasLowerBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.lastCommit), committed.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL
                    )
            );
        }
        if (committed.hasUpperBound()) {
            criteria.add(
                    new KeyValueCriteria<>(
                            root.get(GitRepo_.lastCommit), committed.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL
                    )
            );
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
}
