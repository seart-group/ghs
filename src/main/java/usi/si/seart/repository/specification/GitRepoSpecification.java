package usi.si.seart.repository.specification;

import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import usi.si.seart.model.*;
import usi.si.seart.repository.criteria.Criteria;
import usi.si.seart.repository.criteria.KeyCriteria;
import usi.si.seart.repository.criteria.KeyValueCriteria;
import usi.si.seart.repository.operation.BinaryOperation;
import usi.si.seart.repository.operation.UnaryOperation;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoSpecification implements Specification<GitRepo> {

    Map<String, ?> parameters;

    @Override
    public Predicate toPredicate(
            @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        Predicate[] predicates = parseParameters(root, parameters).stream()
                .map(criteria -> criteria.toPredicate(root, query, criteriaBuilder))
                .toArray(Predicate[]::new);

        query.distinct(true);

        return criteriaBuilder.and(predicates);
    }

    @SuppressWarnings("unchecked")
    public static List<Criteria<GitRepo>> parseParameters(Root<GitRepo> root, Map<String, ?> parameters){
        List<Criteria<GitRepo>> criteria = new ArrayList<>();

        String name = (String) parameters.get("name");
        boolean nameEquals = (Boolean) parameters.get("nameEquals");
        if (StringUtils.isNotBlank(name)) {
            if (nameEquals){
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.EQUAL));
            } else {
                criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.name), name, BinaryOperation.LIKE));
            }
        }

        String language = (String) parameters.get("language");
        if (StringUtils.isNotBlank(language))
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.mainLanguage), language, BinaryOperation.EQUAL));

        String license = (String) parameters.get("license");
        if (StringUtils.isNotBlank(license))
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.license), license, BinaryOperation.EQUAL));

        String label = (String) parameters.get("label");
        if (StringUtils.isNotBlank(label))
            criteria.add(new KeyValueCriteria<>(root.join(GitRepo_.labels).get(GitRepoLabel_.label), label, BinaryOperation.EQUAL));

        Range<Long> commits = (Range<Long>) parameters.get("commits");
        if (commits.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.commits), commits.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (commits.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.commits), commits.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> contributors = (Range<Long>) parameters.get("contributors");
        if (contributors.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.contributors), contributors.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (contributors.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.contributors), contributors.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> issues = (Range<Long>) parameters.get("issues");
        if (issues.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalIssues), issues.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (issues.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalIssues), issues.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> pulls = (Range<Long>) parameters.get("pulls");
        if (pulls.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalPullRequests), pulls.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (pulls.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.totalPullRequests), pulls.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> branches = (Range<Long>) parameters.get("branches");
        if (branches.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.branches), branches.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (branches.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.branches), branches.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> releases = (Range<Long>) parameters.get("releases");
        if (releases.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.releases), releases.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (releases.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.releases), releases.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> stars = (Range<Long>) parameters.get("stars");
        if (stars.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.stargazers), stars.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (stars.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.stargazers), stars.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> watchers = (Range<Long>) parameters.get("watchers");
        if (watchers.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.watchers), watchers.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (watchers.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.watchers), watchers.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> forks = (Range<Long>) parameters.get("forks");
        if (forks.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.forks), forks.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (forks.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.forks), forks.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));


        Path<Long> commentlinesPath;
        Path<Long> codelinesPath;
        Path<Long> totallinesPath;
        if (StringUtils.isNotBlank(language)) {
            criteria.add(new KeyValueCriteria<>(root.join(GitRepo_.metrics).join(GitRepoMetric_.language).get(MetricLanguage_.language), language, BinaryOperation.EQUAL));
            commentlinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.commentLines);
            codelinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.codeLines);
            totallinesPath = root.join(GitRepo_.metrics).get(GitRepoMetric_.totalLines);
        } else {
            commentlinesPath = root.get(GitRepo_.totalCommentLines);
            codelinesPath = root.get(GitRepo_.totalCodeLines);
            totallinesPath = root.get(GitRepo_.totalLines);
        }

        Range<Long> codeLines = (Range<Long>) parameters.get("codeLines");
        if (codeLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(codelinesPath, codeLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (codeLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(codelinesPath, codeLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> commentLines = (Range<Long>) parameters.get("commentLines");
        if (commentLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(commentlinesPath, commentLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (commentLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(commentlinesPath, commentLines.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> totalLines = (Range<Long>) parameters.get("totalLines");
        if (totalLines.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(totallinesPath, totalLines.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (totalLines.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(totallinesPath, totalLines.lowerEndpoint(), BinaryOperation.LESS_THAN_EQUAL));


        Range<Date> created = (Range<Date>) parameters.get("created");
        if (created.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.createdAt), created.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (created.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.createdAt), created.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Date> committed = (Range<Date>) parameters.get("committed");
        if (committed.hasLowerBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.lastCommit), committed.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (committed.hasUpperBound())
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.lastCommit), committed.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        boolean excludeForks = (Boolean) parameters.get("excludeForks");
        boolean onlyForks = (Boolean) parameters.get("onlyForks");
        if (excludeForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), false, BinaryOperation.EQUAL));
        } else if (onlyForks) {
            criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.isFork), true, BinaryOperation.EQUAL));
        }

        boolean hasIssues = (Boolean) parameters.get("hasIssues");
        if (hasIssues) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openIssues), 0L, BinaryOperation.GREATER_THAN));

        boolean hasPulls = (Boolean) parameters.get("hasPulls");
        if (hasPulls) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.openPullRequests), 0L, BinaryOperation.GREATER_THAN));

        boolean hasWiki = (Boolean) parameters.get("hasWiki");
        if (hasWiki) criteria.add(new KeyValueCriteria<>(root.get(GitRepo_.hasWiki), true, BinaryOperation.EQUAL));

        boolean hasLicense = (Boolean) parameters.get("hasLicense");
        if (hasLicense) criteria.add(new KeyCriteria<>(root.get(GitRepo_.license), UnaryOperation.IS_NOT_NULL));

        return criteria;
    }
}
