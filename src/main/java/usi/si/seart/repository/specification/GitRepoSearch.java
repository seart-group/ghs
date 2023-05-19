package usi.si.seart.repository.specification;

import com.google.common.collect.Range;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoLabel_;
import usi.si.seart.model.GitRepoMetric_;
import usi.si.seart.model.GitRepo_;
import usi.si.seart.model.MetricLanguage_;
import usi.si.seart.repository.criteria.Criteria;
import usi.si.seart.repository.criteria.KeyCriteria;
import usi.si.seart.repository.criteria.KeyValueCriteria;
import usi.si.seart.repository.operation.BinaryOperation;
import usi.si.seart.repository.operation.UnaryOperation;

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
    Range<Long> totalLines;
    boolean excludeForks;
    boolean onlyForks;
    boolean hasIssues;
    boolean hasPulls;
    boolean hasWiki;
    boolean hasLicense;

    boolean hasCodeMetricsFilters() {
        return codeLines.hasLowerBound() || codeLines.hasUpperBound() ||
                commentLines.hasLowerBound() || commentLines.hasUpperBound() ||
                totalLines.hasLowerBound() || totalLines.hasUpperBound();
    }

    public List<Criteria<GitRepo>> toCriteriaList(Root<GitRepo> root) {
        List<Criteria<GitRepo>> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(name)) {
            if (nameEquals){
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.EQUAL));
            } else {
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.LIKE));
            }
        }

        if (StringUtils.isNotBlank(language))
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.mainLanguage), language, BinaryOperation.EQUAL));

        if (StringUtils.isNotBlank(license))
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.license), license, BinaryOperation.EQUAL));

        if (StringUtils.isNotBlank(label))
            criteria.add(new KeyValueCriteria<>(root.join(GitRepo_.labels).get(GitRepoLabel_.label), label, BinaryOperation.EQUAL));

        if (commits.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.commits), commits.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (commits.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.commits), commits.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (contributors.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.contributors), contributors.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (contributors.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.contributors), contributors.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (issues.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalIssues), issues.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (issues.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalIssues), issues.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (pulls.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalPullRequests), pulls.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (pulls.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalPullRequests), pulls.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (branches.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.branches), branches.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (branches.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.branches), branches.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (releases.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.releases), releases.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (releases.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.releases), releases.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (stars.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.stargazers), stars.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (stars.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.stargazers), stars.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (watchers.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.watchers), watchers.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (watchers.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.watchers), watchers.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (forks.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.forks), forks.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (forks.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.forks), forks.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));


        Path<Long> commentLinesPath;
        Path<Long> codeLinesPath;
        Path<Long> totalLinesPath;
        if (StringUtils.isNotBlank(language) && hasCodeMetricsFilters()) {
            criteria.add(new KeyValueCriteria<>(root.join(GitRepo_.metrics).join(GitRepoMetric_.language).get(MetricLanguage_.language), language, BinaryOperation.EQUAL));
            commentLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.commentLines);
            codeLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.codeLines);
            totalLinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.totalLines);
        } else {
            commentLinesPath = root.get(GitRepo_.totalCommentLines);
            codeLinesPath = root.get(GitRepo_.totalCodeLines);
            totalLinesPath = root.get(GitRepo_.totalLines);
        }

        if (codeLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(codeLinesPath, codeLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (codeLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(codeLinesPath, codeLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (commentLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(commentLinesPath, commentLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (commentLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(commentLinesPath, commentLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (totalLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(totalLinesPath, totalLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (totalLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(totalLinesPath, totalLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));


        if (created.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.createdAt), created.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (created.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.createdAt), created.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (committed.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.lastCommit), committed.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (committed.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.lastCommit), committed.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        if (excludeForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), false, BinaryOperation.EQUAL));
        } else if (onlyForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), true, BinaryOperation.EQUAL));
        }

        if (hasIssues) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openIssues), 0L, BinaryOperation.GREATER_THAN));

        if (hasPulls) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openPullRequests), 0L, BinaryOperation.GREATER_THAN));

        if (hasWiki) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.hasWiki), true, BinaryOperation.EQUAL));

        if (hasLicense) criteria.add(new KeyCriteria<>(root.get(GitRepo_.license), UnaryOperation.IS_NOT_NULL));

        return criteria;
    }
}
