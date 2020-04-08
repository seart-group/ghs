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

    public List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                        LongInterval commits, LongInterval contributors, LongInterval issues,
                                        LongInterval pulls, LongInterval branches, LongInterval releases,
                                        LongInterval stars, LongInterval watchers, LongInterval forks,
                                        DateInterval created, DateInterval committed, Boolean excludeForks,
                                        Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                        Boolean hasLicense, Pageable pageable){
        JPAQueryBuilder qb = new JPAQueryBuilder();
        qb.select("r",true);
        qb.from("GitRepo", "r");
        qb.join("GitRepoLabel", "rl", "r.id = rl.repo.id" , Join.LEFT);
        qb.orderBy("r.name");

        Map<String,Object> parameters = new HashMap<>();

        if (StringUtils.isNotBlank(name)){
            if (nameEquals){
                qb.where("r.name = (:name)", Operator.AND);
                parameters.put("name", name);
            } else {
                qb.where("lower(r.name) like lower(:name)", Operator.AND);
                parameters.put("name", "%"+name+"%");
            }
        }

        if (StringUtils.isNoneBlank(language)){
            qb.where("r.main_language = (:language)", Operator.AND);
            parameters.put("language", language);
        }

        if (StringUtils.isNoneBlank(license)){
            qb.where("r.license = (:repoLicense)", Operator.AND);
            parameters.put("repoLicense", license);
        }

        if (StringUtils.isNoneBlank(label)){
            qb.where("rl.repo_label_name = (:repoLabel)", Operator.AND);
            parameters.put("repoLabel",label);
        }

        if (commits.isLowerBound()){
            qb.where("r.commits >= (:lower)",Operator.AND);
            parameters.put("lower",commits.getStart());
        } else if (commits.isUpperBound()){
            qb.where("r.commits <= (:upper)",Operator.AND);
            parameters.put("upper",commits.getEnd());
        } else if (commits.isBound()){
            qb.where("r.commits between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",commits.getStart());
            parameters.put("upper",commits.getEnd());
        }

        if (contributors.isLowerBound()){
            qb.where("r.contributors >= (:lower)",Operator.AND);
            parameters.put("lower",contributors.getStart());
        } else if (contributors.isUpperBound()){
            qb.where("r.contributors <= (:upper)",Operator.AND);
            parameters.put("upper",contributors.getEnd());
        } else if (contributors.isBound()){
            qb.where("r.contributors between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",contributors.getStart());
            parameters.put("upper",contributors.getEnd());
        }

        if (issues.isLowerBound()){
            qb.where("r.total_issues >= (:lower)",Operator.AND);
            parameters.put("lower",issues.getStart());
        } else if (issues.isUpperBound()){
            qb.where("r.total_issues <= (:upper)",Operator.AND);
            parameters.put("upper",issues.getEnd());
        } else if (issues.isBound()){
            qb.where("r.total_issues between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",issues.getStart());
            parameters.put("upper",issues.getEnd());
        }

        if (pulls.isLowerBound()){
            qb.where("r.open_pull_requests >= (:lower)",Operator.AND);
            parameters.put("lower",pulls.getStart());
        } else if (pulls.isUpperBound()){
            qb.where("r.open_pull_requests <= (:upper)",Operator.AND);
            parameters.put("upper",pulls.getEnd());
        } else if (pulls.isBound()){
            qb.where("r.open_pull_requests between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",pulls.getStart());
            parameters.put("upper",pulls.getEnd());
        }

        if (branches.isLowerBound()){
            qb.where("r.branches >= (:lower)",Operator.AND);
            parameters.put("lower",branches.getStart());
        } else if (branches.isUpperBound()){
            qb.where("r.branches <= (:upper)",Operator.AND);
            parameters.put("upper",branches.getEnd());
        } else if (branches.isBound()){
            qb.where("r.branches between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",branches.getStart());
            parameters.put("upper",branches.getEnd());
        }

        if (releases.isLowerBound()){
            qb.where("r.releases >= (:lower)",Operator.AND);
            parameters.put("lower",releases.getStart());
        } else if (releases.isUpperBound()){
            qb.where("r.releases <= (:upper)",Operator.AND);
            parameters.put("upper",releases.getEnd());
        } else if (releases.isBound()){
            qb.where("r.releases between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",releases.getStart());
            parameters.put("upper",releases.getEnd());
        }

        if (stars.isLowerBound()){
            qb.where("r.stargazers >= (:lower)",Operator.AND);
            parameters.put("lower",stars.getStart());
        } else if (stars.isUpperBound()){
            qb.where("r.stargazers <= (:upper)",Operator.AND);
            parameters.put("upper",stars.getEnd());
        } else if (stars.isBound()){
            qb.where("r.stargazers between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",stars.getStart());
            parameters.put("upper",stars.getEnd());
        }

        if (watchers.isLowerBound()){
            qb.where("r.watchers >= (:lower)",Operator.AND);
            parameters.put("lower",watchers.getStart());
        } else if (stars.isUpperBound()){
            qb.where("r.watchers <= (:upper)",Operator.AND);
            parameters.put("upper",watchers.getEnd());
        } else if (stars.isBound()){
            qb.where("r.watchers between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",watchers.getStart());
            parameters.put("upper",watchers.getEnd());
        }

        if (forks.isLowerBound()){
            qb.where("r.forks >= (:lower)",Operator.AND);
            parameters.put("lower",forks.getStart());
        } else if (stars.isUpperBound()){
            qb.where("r.forks <= (:upper)",Operator.AND);
            parameters.put("upper",forks.getEnd());
        } else if (stars.isBound()){
            qb.where("r.forks between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",forks.getStart());
            parameters.put("upper",forks.getEnd());
        }

        if (created.isLowerBound()){
            qb.where("date(r.created_at) >= (:lower)",Operator.AND);
            parameters.put("lower",created.getStart());
        } else if (stars.isUpperBound()){
            qb.where("date(r.created_at) <= (:upper)",Operator.AND);
            parameters.put("upper",created.getEnd());
        } else if (stars.isBound()){
            qb.where("date(r.created_at) between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",created.getStart());
            parameters.put("upper",created.getEnd());
        }

        if (committed.isLowerBound()){
            qb.where("date(r.pushed_at) >= (:lower)",Operator.AND);
            parameters.put("lower",committed.getStart());
        } else if (stars.isUpperBound()){
            qb.where("date(r.pushed_at) <= (:upper)",Operator.AND);
            parameters.put("upper",committed.getEnd());
        } else if (stars.isBound()){
            qb.where("date(r.pushed_at) between (:lower) and (:upper)",Operator.AND);
            parameters.put("lower",committed.getStart());
            parameters.put("upper",committed.getEnd());
        }

        if (excludeForks){
            qb.where("r.isFork = false",Operator.AND);
        }

        if (onlyForks){
            qb.where("r.isFork = true",Operator.AND);
        }

        if (hasIssues){
            qb.where("r.open_issues > 0",Operator.AND);
        }

        if (hasPulls){
            qb.where("r.open_pull_requests > 0",Operator.AND);
        }

        if (hasWiki){
            qb.where("r.has_wiki = true",Operator.AND);
        }

        if (hasLicense){
            qb.where("r.license is not null",Operator.AND);
        }

        TypedQuery<GitRepo> query = entityManager.createQuery(qb.build(), GitRepo.class);
        query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
        query.setMaxResults(pageable.getPageSize());
        parameters.keySet().forEach(k -> query.setParameter(k, parameters.get(k)));
        return query.getResultList();
    }
}
