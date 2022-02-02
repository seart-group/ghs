package usi.si.seart.gseapp.repository.specification;

import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import usi.si.seart.gseapp.model.GitRepo;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoSpecification implements Specification<GitRepo> {

    List<SearchCriteria> criteriaList = new ArrayList<>();

    public void add(SearchCriteria criteria) {
        criteriaList.add(criteria);
    }

    @Override
    public Predicate toPredicate(
            @NotNull Root<GitRepo> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : criteriaList) {
            String key = criteria.getKey();
            Object value = criteria.getValue();
            SearchOperation operation = criteria.getOperation();

            switch (operation) {
                case GREATER_THAN:
                    predicates.add(criteriaBuilder.greaterThan(root.get(key), value.toString()));
                    break;
                case GREATER_THAN_EQUAL:
                    if (value instanceof Date) {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Date) value));
                    } else {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), value.toString()));
                    }
                    break;
                case LESS_THAN_EQUAL:
                    if (value instanceof Date) {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(key), (Date) value));
                    } else {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(key), value.toString()));
                    }
                    break;
                case EQUAL:
                    predicates.add(criteriaBuilder.equal(root.get(key), value));
                    break;
                case LIKE:
                    predicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(key)),
                                    "%" + value.toString().toLowerCase() + "%"
                            )
                    );
                    break;
                case IN:
                    predicates.add(criteriaBuilder.equal(root.join(key).get("label"), value.toString()));
                    break;
                case IS_NOT_NULL:
                    predicates.add(criteriaBuilder.isNotNull(root.get(key)));
                    break;
            }
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    @SuppressWarnings("rawtypes")
    public static GitRepoSpecification from(Map<String, ?> parameters){
        GitRepoSpecification specification = new GitRepoSpecification();

        String name = (String) parameters.get("name");
        Boolean nameEquals = (Boolean) parameters.get("nameEquals");
        if (StringUtils.isNotBlank(name)) {
            if (nameEquals){
                specification.add(new SearchCriteria("name", name, SearchOperation.EQUAL));
            } else {
                specification.add(new SearchCriteria("name", name, SearchOperation.LIKE));
            }
        }

        String language = (String) parameters.get("language");
        if (StringUtils.isNoneBlank(language))
            specification.add(new SearchCriteria("mainLanguage", language, SearchOperation.EQUAL));

        String license = (String) parameters.get("license");
        if (StringUtils.isNoneBlank(license))
            specification.add(new SearchCriteria("license", license, SearchOperation.EQUAL));

        String label = (String) parameters.get("label");
        if (StringUtils.isNoneBlank(label))
            specification.add(new SearchCriteria("labels", label, SearchOperation.IN));

        Range commits = (Range) parameters.get("commits");
        if (commits.hasLowerBound())
            specification.add(new SearchCriteria("commits", commits.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (commits.hasUpperBound())
            specification.add(new SearchCriteria("commits", commits.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range contributors = (Range) parameters.get("contributors");
        if (contributors.hasLowerBound())
            specification.add(new SearchCriteria("contributors", contributors.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (contributors.hasUpperBound())
            specification.add(new SearchCriteria("contributors", contributors.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range issues = (Range) parameters.get("issues");
        if (issues.hasLowerBound())
            specification.add(new SearchCriteria("totalIssues", issues.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (issues.hasUpperBound())
            specification.add(new SearchCriteria("totalIssues", issues.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range pulls = (Range) parameters.get("pulls");
        if (pulls.hasLowerBound())
            specification.add(new SearchCriteria("totalPullRequests", pulls.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (pulls.hasUpperBound())
            specification.add(new SearchCriteria("totalPullRequests", pulls.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range branches = (Range) parameters.get("branches");
        if (branches.hasLowerBound())
            specification.add(new SearchCriteria("branches", branches.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (branches.hasUpperBound())
            specification.add(new SearchCriteria("branches", branches.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range releases = (Range) parameters.get("releases");
        if (releases.hasLowerBound())
            specification.add(new SearchCriteria("releases", releases.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (releases.hasUpperBound())
            specification.add(new SearchCriteria("releases", releases.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range stars = (Range) parameters.get("stars");
        if (stars.hasLowerBound())
            specification.add(new SearchCriteria("stargazers", stars.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (stars.hasUpperBound())
            specification.add(new SearchCriteria("stargazers", stars.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range watchers = (Range) parameters.get("watchers");
        if (watchers.hasLowerBound())
            specification.add(new SearchCriteria("watchers", watchers.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (watchers.hasUpperBound())
            specification.add(new SearchCriteria("watchers", watchers.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range forks = (Range) parameters.get("forks");
        if (forks.hasLowerBound())
            specification.add(new SearchCriteria("forks", forks.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (forks.hasUpperBound())
            specification.add(new SearchCriteria("forks", forks.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range created = (Range) parameters.get("created");
        if (created.hasLowerBound())
            specification.add(new SearchCriteria("createdAt", created.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (created.hasUpperBound())
            specification.add(new SearchCriteria("createdAt", created.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Range committed = (Range) parameters.get("committed");
        if (committed.hasLowerBound())
            specification.add(new SearchCriteria("pushedAt", committed.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (committed.hasUpperBound())
            specification.add(new SearchCriteria("pushedAt", committed.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        Boolean excludeForks = (Boolean) parameters.get("excludeForks");
        Boolean onlyForks = (Boolean) parameters.get("onlyForks");
        if (excludeForks) {
            specification.add(new SearchCriteria("isFork", false, SearchOperation.EQUAL));
        } else if (onlyForks) {
            specification.add(new SearchCriteria("isFork", true, SearchOperation.EQUAL));
        }

        Boolean hasIssues = (Boolean) parameters.get("hasIssues");
        if (hasIssues) specification.add(new SearchCriteria("openIssues", 0, SearchOperation.GREATER_THAN));

        Boolean hasPulls = (Boolean) parameters.get("hasPulls");
        if (hasPulls) specification.add(new SearchCriteria("openPullRequests", 0, SearchOperation.GREATER_THAN));

        Boolean hasWiki = (Boolean) parameters.get("hasWiki");
        if (hasWiki) specification.add(new SearchCriteria("hasWiki", true, SearchOperation.EQUAL));

        Boolean hasLicense = (Boolean) parameters.get("hasLicense");
        if (hasLicense) specification.add(new SearchCriteria("license", new Object(), SearchOperation.IS_NOT_NULL));

        return specification;
    }
}
