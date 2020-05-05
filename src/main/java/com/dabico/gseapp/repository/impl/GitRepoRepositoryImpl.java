package com.dabico.gseapp.repository.impl;

import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.repository.GitRepoRepositoryCustom;
import com.dabico.gseapp.repository.util.JPAQueryBuilder;
import com.dabico.gseapp.repository.util.Join;
import com.dabico.gseapp.repository.util.Operator;
import com.dabico.gseapp.util.interval.DateInterval;
import com.dabico.gseapp.util.interval.LongInterval;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoRepositoryImpl implements GitRepoRepositoryCustom {

    EntityManager entityManager;

    public Long countResults(String name, Boolean nameEquals, String language, String license, String label,
                             LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
                             LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
                             LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
                             Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                             Boolean hasLicense)
    {
        TypedQuery<Long> counter = constructCounter(name,nameEquals,language,license,label,commits,contributors,issues,
                                                    pulls,branches,releases,stars,watchers,forks,created,committed,
                                                    excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,hasLicense);
        Map<String,Object> parameters = constructParameterMap(name,nameEquals,language,license,label,commits,
                                                              contributors,issues,pulls,branches,releases,stars,
                                                              watchers,forks,created,committed);
        parameters.keySet().forEach(k -> counter.setParameter(k, parameters.get(k)));
        return counter.getSingleResult();
    }

    public List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                        LongInterval commits, LongInterval contributors, LongInterval issues,
                                        LongInterval pulls, LongInterval branches, LongInterval releases,
                                        LongInterval stars, LongInterval watchers, LongInterval forks,
                                        DateInterval created, DateInterval committed, Boolean excludeForks,
                                        Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                        Boolean hasLicense, Pageable pageable)
    {
        TypedQuery<GitRepo> query = constructQuery(name,nameEquals,language,license,label,commits,contributors,issues,
                                                   pulls,branches,releases,stars,watchers,forks,created,committed,
                                                   excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,hasLicense);
        Map<String,Object> parameters = constructParameterMap(name,nameEquals,language,license,label,commits,contributors,issues,
                                                        pulls,branches,releases,stars,watchers,forks,created,committed);
        query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
        parameters.keySet().forEach(k -> query.setParameter(k, parameters.get(k)));
        return query.getResultList();
    }

    public List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                        LongInterval commits, LongInterval contributors, LongInterval issues,
                                        LongInterval pulls, LongInterval branches, LongInterval releases,
                                        LongInterval stars, LongInterval watchers, LongInterval forks,
                                        DateInterval created, DateInterval committed, Boolean excludeForks,
                                        Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                        Boolean hasLicense)
    {
        TypedQuery<GitRepo> query = constructQuery(name,nameEquals,language,license,label,commits,contributors,issues,
                                                   pulls,branches,releases,stars,watchers,forks,created,committed,
                                                   excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,hasLicense);
        Map<String,Object> parameters = constructParameterMap(name,nameEquals,language,license,label,commits,contributors,issues,
                                                        pulls,branches,releases,stars,watchers,forks,created,committed);
        parameters.keySet().forEach(k -> query.setParameter(k, parameters.get(k)));
        return query.getResultList();
    }

    private TypedQuery<Long> constructCounter(String name, Boolean nameEquals, String language, String license,
                                              String label, LongInterval commits, LongInterval contributors,
                                              LongInterval issues, LongInterval pulls, LongInterval branches,
                                              LongInterval releases, LongInterval stars, LongInterval watchers,
                                              LongInterval forks, DateInterval created, DateInterval committed,
                                              Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
                                              Boolean hasPulls, Boolean hasWiki, Boolean hasLicense)
    {
        JPAQueryBuilder qb = constructQueryParameters(name,nameEquals,language,license,label, commits, contributors,
                                                      issues,pulls,branches,releases,stars,watchers,forks,created,
                                                      committed,excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,
                                                      hasLicense);
        qb.select("count(distinct r)",false);
        return entityManager.createQuery(qb.build(), Long.class);
    }

    private TypedQuery<GitRepo> constructQuery(String name, Boolean nameEquals, String language, String license,
                                               String label, LongInterval commits, LongInterval contributors,
                                               LongInterval issues, LongInterval pulls, LongInterval branches,
                                               LongInterval releases, LongInterval stars, LongInterval watchers,
                                               LongInterval forks, DateInterval created, DateInterval committed,
                                               Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
                                               Boolean hasPulls, Boolean hasWiki, Boolean hasLicense)
    {
        JPAQueryBuilder qb = constructQueryParameters(name,nameEquals,language,license,label, commits, contributors,
                                                      issues,pulls,branches,releases,stars,watchers,forks,created,
                                                      committed,excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,
                                                      hasLicense);
        qb.select("r",true);
        qb.orderBy("r.name");
        return entityManager.createQuery(qb.build(), GitRepo.class);
    }

    private JPAQueryBuilder constructQueryParameters(String name, Boolean nameEquals, String language, String license,
                                                     String label, LongInterval commits, LongInterval contributors,
                                                     LongInterval issues, LongInterval pulls, LongInterval branches,
                                                     LongInterval releases, LongInterval stars, LongInterval watchers,
                                                     LongInterval forks, DateInterval created, DateInterval committed,
                                                     Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
                                                     Boolean hasPulls, Boolean hasWiki, Boolean hasLicense)
    {
        JPAQueryBuilder qb = new JPAQueryBuilder();
        qb.from("GitRepo", "r");
        qb.join("GitRepoLabel", "rl", "r.id = rl.repo.id" , Join.LEFT);

        if (StringUtils.isNotBlank(name)){
            if (nameEquals){
                qb.where("r.name = (:name)", Operator.AND);
            } else {
                qb.where("lower(r.name) like lower(:name)", Operator.AND);
            }
        }

        if (StringUtils.isNoneBlank(language)){
            qb.where("r.mainLanguage = (:language)", Operator.AND);
        }

        if (StringUtils.isNoneBlank(license)){
            qb.where("r.license = (:license)", Operator.AND);
        }

        if (StringUtils.isNoneBlank(label)){
            qb.where("rl.label = (:label)", Operator.AND);
        }

        if (commits.isLowerBound()){
            qb.where("r.commits >= (:commitsMin)",Operator.AND);
        } else if (commits.isUpperBound()){
            qb.where("r.commits <= (:commitsMax)",Operator.AND);
        } else if (commits.isBound()){
            qb.where("r.commits between (:commitsMin) and (:commitsMax)",Operator.AND);
        }

        if (contributors.isLowerBound()){
            qb.where("r.contributors >= (:contributorsMin)",Operator.AND);
        } else if (contributors.isUpperBound()){
            qb.where("r.contributors <= (:contributorsMax)",Operator.AND);
        } else if (contributors.isBound()){
            qb.where("r.contributors between (:contributorsMin) and (:contributorsMax)",Operator.AND);
        }

        if (issues.isLowerBound()){
            qb.where("r.totalIssues >= (:issuesMin)",Operator.AND);
        } else if (issues.isUpperBound()){
            qb.where("r.totalIssues <= (:issuesMax)",Operator.AND);
        } else if (issues.isBound()){
            qb.where("r.totalIssues between (:issuesMin) and (:issuesMax)",Operator.AND);
        }

        if (pulls.isLowerBound()){
            qb.where("r.totalPullRequests >= (:pullsMin)",Operator.AND);
        } else if (pulls.isUpperBound()){
            qb.where("r.totalPullRequests <= (:pullsMax)",Operator.AND);
        } else if (pulls.isBound()){
            qb.where("r.totalPullRequests between (:pullsMin) and (:pullsMax)",Operator.AND);
        }

        if (branches.isLowerBound()){
            qb.where("r.branches >= (:branchesMin)",Operator.AND);
        } else if (branches.isUpperBound()){
            qb.where("r.branches <= (:branchesMax)",Operator.AND);
        } else if (branches.isBound()){
            qb.where("r.branches between (:branchesMin) and (:branchesMax)",Operator.AND);
        }

        if (releases.isLowerBound()){
            qb.where("r.releases >= (:releasesMin)",Operator.AND);
        } else if (releases.isUpperBound()){
            qb.where("r.releases <= (:releasesMax)",Operator.AND);
        } else if (releases.isBound()){
            qb.where("r.releases between (:releasesMin) and (:releasesMax)",Operator.AND);
        }

        if (stars.isLowerBound()){
            qb.where("r.stargazers >= (:starsMin)",Operator.AND);
        } else if (stars.isUpperBound()){
            qb.where("r.stargazers <= (:starsMax)",Operator.AND);
        } else if (stars.isBound()){
            qb.where("r.stargazers between (:starsMin) and (:starsMax)",Operator.AND);
        }

        if (watchers.isLowerBound()){
            qb.where("r.watchers >= (:watchersMin)",Operator.AND);
        } else if (watchers.isUpperBound()){
            qb.where("r.watchers <= (:watchersMax)",Operator.AND);
        } else if (watchers.isBound()){
            qb.where("r.watchers between (:watchersMin) and (:watchersMax)",Operator.AND);
        }

        if (forks.isLowerBound()){
            qb.where("r.forks >= (:forksMin)",Operator.AND);
        } else if (forks.isUpperBound()){
            qb.where("r.forks <= (:forksMax)",Operator.AND);
        } else if (forks.isBound()){
            qb.where("r.forks between (:forksMin) and (:forksMax)",Operator.AND);
        }

        if (created.isLowerBound()){
            qb.where("date(r.createdAt) >= (:createdMin)",Operator.AND);
        } else if (created.isUpperBound()){
            qb.where("date(r.createdAt) <= (:createdMax)",Operator.AND);
        } else if (created.isBound()){
            qb.where("date(r.createdAt) between (:createdMin) and (:createdMax)",Operator.AND);
        }

        if (committed.isLowerBound()){
            qb.where("date(r.pushedAt) >= (:pushedMin)",Operator.AND);
        } else if (committed.isUpperBound()){
            qb.where("date(r.pushedAt) <= (:pushedMax)",Operator.AND);
        } else if (committed.isBound()){
            qb.where("date(r.pushedAt) between (:pushedMin) and (:pushedMax)",Operator.AND);
        }

        if (excludeForks){
            qb.where("r.isFork = false",Operator.AND);
        }

        if (onlyForks){
            qb.where("r.isFork = true",Operator.AND);
        }

        if (hasIssues){
            qb.where("r.openIssues > 0",Operator.AND);
        }

        if (hasPulls){
            qb.where("r.openPullRequests > 0",Operator.AND);
        }

        if (hasWiki){
            qb.where("r.hasWiki is true",Operator.AND);
        }

        if (hasLicense){
            qb.where("r.license is not null",Operator.AND);
        }

        return qb;
    }

    private Map<String,Object> constructParameterMap(String name, Boolean nameEquals, String language, String license,
                                                     String label, LongInterval commits, LongInterval contributors,
                                                     LongInterval issues, LongInterval pulls, LongInterval branches,
                                                     LongInterval releases, LongInterval stars, LongInterval watchers,
                                                     LongInterval forks, DateInterval created, DateInterval committed)
    {
        Map<String,Object> parameters = new HashMap<>();

        if (StringUtils.isNotBlank(name)){
            if (nameEquals){
                parameters.put("name", name);
            } else {
                parameters.put("name", "%"+name+"%");
            }
        }

        if (StringUtils.isNotBlank(language)){
            parameters.put("language", language);
        }

        if (StringUtils.isNotBlank(license)){
            parameters.put("license", license);
        }

        if (StringUtils.isNotBlank(label)){
            parameters.put("label",label);
        }

        if (commits.isLowerBound()){
            parameters.put("commitsMin",commits.getStart());
        } else if (commits.isUpperBound()){
            parameters.put("commitsMax",commits.getEnd());
        } else if (commits.isBound()){
            parameters.put("commitsMin",commits.getStart());
            parameters.put("commitsMax",commits.getEnd());
        }

        if (contributors.isLowerBound()){
            parameters.put("contributorsMin",contributors.getStart());
        } else if (contributors.isUpperBound()){
            parameters.put("contributorsMax",contributors.getEnd());
        } else if (contributors.isBound()){
            parameters.put("contributorsMin",contributors.getStart());
            parameters.put("contributorsMax",contributors.getEnd());
        }

        if (issues.isLowerBound()){
            parameters.put("issuesMin",issues.getStart());
        } else if (issues.isUpperBound()){
            parameters.put("issuesMax",issues.getEnd());
        } else if (issues.isBound()){
            parameters.put("issuesMin",issues.getStart());
            parameters.put("issuesMax",issues.getEnd());
        }

        if (pulls.isLowerBound()){
            parameters.put("pullsMin",pulls.getStart());
        } else if (pulls.isUpperBound()){
            parameters.put("pullsMax",pulls.getEnd());
        } else if (pulls.isBound()){
            parameters.put("pullsMin",pulls.getStart());
            parameters.put("pullsMax",pulls.getEnd());
        }

        if (branches.isLowerBound()){
            parameters.put("branchesMin",branches.getStart());
        } else if (branches.isUpperBound()){
            parameters.put("branchesMax",branches.getEnd());
        } else if (branches.isBound()){
            parameters.put("branchesMin",branches.getStart());
            parameters.put("branchesMax",branches.getEnd());
        }

        if (releases.isLowerBound()){
            parameters.put("releasesMin",releases.getStart());
        } else if (releases.isUpperBound()){
            parameters.put("releasesMax",releases.getEnd());
        } else if (releases.isBound()){
            parameters.put("releasesMin",releases.getStart());
            parameters.put("releasesMax",releases.getEnd());
        }

        if (stars.isLowerBound()){
            parameters.put("starsMin",stars.getStart());
        } else if (stars.isUpperBound()){
            parameters.put("starsMax",stars.getEnd());
        } else if (stars.isBound()){
            parameters.put("starsMin",stars.getStart());
            parameters.put("starsMax",stars.getEnd());
        }

        if (watchers.isLowerBound()){
            parameters.put("watchersMin",watchers.getStart());
        } else if (watchers.isUpperBound()){
            parameters.put("watchersMax",watchers.getEnd());
        } else if (watchers.isBound()){
            parameters.put("watchersMin",watchers.getStart());
            parameters.put("watchersMax",watchers.getEnd());
        }

        if (forks.isLowerBound()){
            parameters.put("forksMin",forks.getStart());
        } else if (forks.isUpperBound()){
            parameters.put("forksMax",forks.getEnd());
        } else if (forks.isBound()){
            parameters.put("forksMin",forks.getStart());
            parameters.put("forksMax",forks.getEnd());
        }

        if (created.isLowerBound()){
            parameters.put("createdMin",created.getStart());
        } else if (created.isUpperBound()){
            parameters.put("createdMax",created.getEnd());
        } else if (created.isBound()){
            parameters.put("createdMin",created.getStart());
            parameters.put("createdMax",created.getEnd());
        }

        if (committed.isLowerBound()){
            parameters.put("pushedMin",committed.getStart());
        } else if (committed.isUpperBound()){
            parameters.put("pushedMax",committed.getEnd());
        } else if (committed.isBound()){
            parameters.put("pushedMin",committed.getStart());
            parameters.put("pushedMax",committed.getEnd());
        }

        return parameters;
    }
}
