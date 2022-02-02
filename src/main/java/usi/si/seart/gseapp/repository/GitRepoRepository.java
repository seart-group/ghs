package usi.si.seart.gseapp.repository;

import com.google.common.collect.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.repository.specification.GitRepoSpecification;
import usi.si.seart.gseapp.repository.specification.SearchCriteria;
import usi.si.seart.gseapp.repository.specification.SearchOperation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long>, JpaSpecificationExecutor<GitRepo> {
    Optional<GitRepo> findGitRepoByName(String name);
    @Query("select distinct r.mainLanguage,count(r) from GitRepo r group by r.mainLanguage order by count(r) desc")
    List<Object[]> getLanguageStatistics();
    @Query("select distinct r.license from GitRepo r where r.license is not null group by r.license order by count(r.license) desc")
    List<String> findAllLicenses();
    @Query("SELECT r.name FROM GitRepo r ORDER BY r.crawled ASC")
    List<String> findAllRepoNames();

    default Page<GitRepo> findGitRepoDynamically(
            String name, Boolean nameEquals, String language, String license, String label, Range<Long> commits,
            Range<Long> contributors, Range<Long> issues, Range<Long> pulls, Range<Long> branches, Range<Long> releases,
            Range<Long> stars, Range<Long> watchers, Range<Long> forks, Range<Date> created, Range<Date> committed,
            Boolean excludeForks, Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
            Boolean hasLicense, Pageable pageable
    ) {
        GitRepoSpecification specification = new GitRepoSpecification();

        if (StringUtils.isNotBlank(name)) {
            if (nameEquals){
                specification.add(new SearchCriteria("name", name, SearchOperation.EQUAL));
            } else {
                specification.add(new SearchCriteria("name", name, SearchOperation.LIKE));
            }
        }

        if (StringUtils.isNoneBlank(language))
            specification.add(new SearchCriteria("mainLanguage", language, SearchOperation.EQUAL));

        if (StringUtils.isNoneBlank(license))
            specification.add(new SearchCriteria("license", license, SearchOperation.EQUAL));

        if (StringUtils.isNoneBlank(label))
            specification.add(new SearchCriteria("labels", label, SearchOperation.IN));

        if (commits.hasLowerBound())
            specification.add(new SearchCriteria("commits", commits.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (commits.hasUpperBound())
            specification.add(new SearchCriteria("commits", commits.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (contributors.hasLowerBound())
            specification.add(new SearchCriteria("contributors", contributors.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (contributors.hasUpperBound())
            specification.add(new SearchCriteria("contributors", contributors.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (issues.hasLowerBound())
            specification.add(new SearchCriteria("totalIssues", issues.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (issues.hasUpperBound())
            specification.add(new SearchCriteria("totalIssues", issues.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (pulls.hasLowerBound())
            specification.add(new SearchCriteria("totalPullRequests", pulls.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (pulls.hasUpperBound())
            specification.add(new SearchCriteria("totalPullRequests", pulls.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (branches.hasLowerBound())
            specification.add(new SearchCriteria("branches", branches.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (branches.hasUpperBound())
            specification.add(new SearchCriteria("branches", branches.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (releases.hasLowerBound())
            specification.add(new SearchCriteria("releases", releases.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (releases.hasUpperBound())
            specification.add(new SearchCriteria("releases", releases.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (stars.hasLowerBound())
            specification.add(new SearchCriteria("stargazers", stars.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (stars.hasUpperBound())
            specification.add(new SearchCriteria("stargazers", stars.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (watchers.hasLowerBound())
            specification.add(new SearchCriteria("watchers", watchers.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (watchers.hasUpperBound())
            specification.add(new SearchCriteria("watchers", watchers.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (forks.hasLowerBound())
            specification.add(new SearchCriteria("forks", forks.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (forks.hasUpperBound())
            specification.add(new SearchCriteria("forks", forks.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (created.hasLowerBound())
            specification.add(new SearchCriteria("createdAt", created.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (created.hasUpperBound())
            specification.add(new SearchCriteria("createdAt", created.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (committed.hasLowerBound())
            specification.add(new SearchCriteria("pushedAt", committed.lowerEndpoint(), SearchOperation.GREATER_THAN_EQUAL));
        if (committed.hasUpperBound())
            specification.add(new SearchCriteria("pushedAt", committed.upperEndpoint(), SearchOperation.LESS_THAN_EQUAL));

        if (excludeForks) {
            specification.add(new SearchCriteria("isFork", false, SearchOperation.EQUAL));
        } else if (onlyForks) {
            specification.add(new SearchCriteria("isFork", true, SearchOperation.EQUAL));
        }

        if (hasIssues) specification.add(new SearchCriteria("openIssues", 0, SearchOperation.GREATER_THAN));

        if (hasPulls) specification.add(new SearchCriteria("openPullRequests", 0, SearchOperation.GREATER_THAN));

        if (hasWiki) specification.add(new SearchCriteria("hasWiki", true, SearchOperation.EQUAL));

        if (hasLicense) specification.add(new SearchCriteria("license", new Object(), SearchOperation.IS_NOT_NULL));

        return findAll(specification, pageable);
    }
}
