package usi.si.seart.repository.specification;

import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoLabel;
import usi.si.seart.model.GitRepoLabel_;
import usi.si.seart.model.GitRepo_;
import usi.si.seart.repository.criteria.Criteria;
import usi.si.seart.repository.criteria.KeyCriteria;
import usi.si.seart.repository.criteria.KeyValueCriteria;
import usi.si.seart.repository.criteria.NestedKeyValueCriteria;
import usi.si.seart.repository.operation.BinaryOperation;
import usi.si.seart.repository.operation.UnaryOperation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoSpecification implements Specification<GitRepo> {

    List<Criteria> criteriaList = new ArrayList<>();

    private void add(Criteria criteria) {
        criteriaList.add(criteria);
    }

    @Override
    public Predicate toPredicate(
            @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (Criteria criteria : criteriaList) {
            handle(predicates, root, criteriaBuilder, criteria);
        }

        query.distinct(true);

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void handle(
            List<Predicate> predicates, Root<GitRepo> root, CriteriaBuilder criteriaBuilder, Criteria criteria
    ) {
        predicates.addAll(criteria.expand(root, criteriaBuilder));
    }

    @SuppressWarnings("rawtypes")
    public static GitRepoSpecification from(Map<String, ?> parameters) {
        GitRepoSpecification specification = new GitRepoSpecification();

        String name = (String) parameters.get("name");
        boolean nameEquals = (Boolean) parameters.get("nameEquals");
        if (StringUtils.isNotBlank(name)) {
            if (nameEquals) {
                specification.add(new KeyValueCriteria<>(GitRepo_.name, name, BinaryOperation.EQUAL));
            } else {
                specification.add(new KeyValueCriteria<>(GitRepo_.name, name, BinaryOperation.LIKE));
            }
        }

        String language = (String) parameters.get("language");
        if (StringUtils.isNotBlank(language))
            specification.add(new KeyValueCriteria<>(GitRepo_.mainLanguage, language, BinaryOperation.EQUAL));

        String license = (String) parameters.get("license");
        if (StringUtils.isNotBlank(license))
            specification.add(new KeyValueCriteria<>(GitRepo_.license, license, BinaryOperation.EQUAL));

        String label = (String) parameters.get("label");
        if (StringUtils.isNotBlank(label))
            specification.add(new NestedKeyValueCriteria<GitRepo, GitRepoLabel>((Attribute)GitRepo_.labels,
                    new KeyValueCriteria<>(GitRepoLabel_.label, label, BinaryOperation.IN)));

        Range<Long> commits = (Range) parameters.get("commits");
        if (commits.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.commits, commits.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (commits.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.commits, commits.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> contributors = (Range) parameters.get("contributors");
        if (contributors.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.contributors, contributors.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (contributors.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.contributors, contributors.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> issues = (Range) parameters.get("issues");
        if (issues.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.totalIssues, issues.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (issues.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.totalIssues, issues.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> pulls = (Range) parameters.get("pulls");
        if (pulls.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.totalPullRequests, pulls.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (pulls.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.totalPullRequests, pulls.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> branches = (Range) parameters.get("branches");
        if (branches.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.branches, branches.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (branches.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.branches, branches.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> releases = (Range) parameters.get("releases");
        if (releases.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.releases, releases.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (releases.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.releases, releases.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> stars = (Range) parameters.get("stars");
        if (stars.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.stargazers, stars.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (stars.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.stargazers, stars.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Long> watchers = (Range) parameters.get("watchers");
        if (watchers.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.watchers, watchers.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (watchers.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.watchers, watchers.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range forks = (Range) parameters.get("forks");
        if (forks.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.forks, (Long) forks.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (forks.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.forks, (Long) forks.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Date> created = (Range) parameters.get("created");
        if (created.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.createdAt, created.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (created.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.createdAt, created.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        Range<Date> committed = (Range) parameters.get("committed");
        if (committed.hasLowerBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.lastCommit, committed.lowerEndpoint(), BinaryOperation.GREATER_THAN_EQUAL));
        if (committed.hasUpperBound())
            specification.add(new KeyValueCriteria<>(GitRepo_.lastCommit, committed.upperEndpoint(), BinaryOperation.LESS_THAN_EQUAL));

        boolean excludeForks = (Boolean) parameters.get("excludeForks");
        boolean onlyForks = (Boolean) parameters.get("onlyForks");
        if (excludeForks) {
            specification.add(new KeyValueCriteria<>(GitRepo_.isFork, false, BinaryOperation.EQUAL));
        } else if (onlyForks) {
            specification.add(new KeyValueCriteria<>(GitRepo_.isFork, true, BinaryOperation.EQUAL));
        }

        boolean hasIssues = (Boolean) parameters.get("hasIssues");
        if (hasIssues) specification.add(new KeyValueCriteria<>(GitRepo_.openIssues, 0L, BinaryOperation.GREATER_THAN));

        boolean hasPulls = (Boolean) parameters.get("hasPulls");
        if (hasPulls) specification.add(new KeyValueCriteria<>(GitRepo_.openPullRequests, 0L, BinaryOperation.GREATER_THAN));

        boolean hasWiki = (Boolean) parameters.get("hasWiki");
        if (hasWiki) specification.add(new KeyValueCriteria<>(GitRepo_.hasWiki, true, BinaryOperation.EQUAL));

        boolean hasLicense = (Boolean) parameters.get("hasLicense");
        if (hasLicense) specification.add(new KeyCriteria<>(GitRepo_.license, UnaryOperation.IS_NOT_NULL));

        return specification;
    }
}
