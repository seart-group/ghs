package com.dabico.gseapp.repository.impl;

import com.dabico.gseapp.dto.GitRepoDto;
import com.dabico.gseapp.dto.GitRepoLabelDto;
import com.dabico.gseapp.dto.GitRepoLanguageDto;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.GitRepoRepositoryCustom;
import com.dabico.gseapp.repository.util.JPAQueryBuilder;
import com.dabico.gseapp.repository.util.Join;
import com.dabico.gseapp.repository.util.Operator;
import com.dabico.gseapp.util.DateUtils;
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
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoRepositoryCustomImpl implements GitRepoRepositoryCustom {

    EntityManager entityManager;

//    public Long countResults(String name, Boolean nameEquals, String language, String license, String label,
//                             LongInterval commits, LongInterval contributors, LongInterval issues, LongInterval pulls,
//                             LongInterval branches, LongInterval releases, LongInterval stars, LongInterval watchers,
//                             LongInterval forks, DateInterval created, DateInterval committed, Boolean excludeForks,
//                             Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
//                             Boolean hasLicense) {
//        TypedQuery<Long> counter = constructCountQuery(name, nameEquals, language, license, label, commits, contributors, issues,
//                pulls, branches, releases, stars, watchers, forks, created, committed,
//                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense);
//        Map<String, Object> parameters = constructParameterMap(name, nameEquals, language, license, label, commits,
//                contributors, issues, pulls, branches, releases, stars,
//                watchers, forks, created, committed);
//        parameters.keySet().forEach(k -> counter.setParameter(k, parameters.get(k)));
//
//        return counter.getSingleResult();
//    }

//    public List<GitRepo> advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
//                                        LongInterval commits, LongInterval contributors, LongInterval issues,
//                                        LongInterval pulls, LongInterval branches, LongInterval releases,
//                                        LongInterval stars, LongInterval watchers, LongInterval forks,
//                                        DateInterval created, DateInterval committed, Boolean excludeForks,
//                                        Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
//                                        Boolean hasLicense, Pageable pageable) {
//        TypedQuery<GitRepo> query = constructSearchQuery(name, nameEquals, language, license, label, commits, contributors, issues,
//                pulls, branches, releases, stars, watchers, forks, created, committed,
//                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense);
//        Map<String, Object> parameters = constructParameterMap(name, nameEquals, language, license, label, commits, contributors, issues,
//                pulls, branches, releases, stars, watchers, forks, created, committed);
//        if (pageable != null) {
//            query.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
//            query.setMaxResults(pageable.getPageSize());
//        }
//        parameters.keySet().forEach(k -> query.setParameter(k, parameters.get(k)));
//        return query.getResultList();
//    }

//    private TypedQuery<Long> constructCountQuery(String name, Boolean nameEquals, String language, String license,
//                                                 String label, LongInterval commits, LongInterval contributors,
//                                                 LongInterval issues, LongInterval pulls, LongInterval branches,
//                                                 LongInterval releases, LongInterval stars, LongInterval watchers,
//                                                 LongInterval forks, DateInterval created, DateInterval committed,
//                                                 Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
//                                                 Boolean hasPulls, Boolean hasWiki, Boolean hasLicense) {
//        JPAQueryBuilder qb = constructQueryParameters(name, nameEquals, language, license, label, commits, contributors,
//                issues, pulls, branches, releases, stars, watchers, forks, created,
//                committed, excludeForks, onlyForks, hasIssues, hasPulls, hasWiki,
//                hasLicense);
//        qb.select("count(distinct r)", false);
//        return entityManager.createQuery(qb.build(), Long.class);
//    }

//    private TypedQuery<GitRepo> constructSearchQuery(String name, Boolean nameEquals, String language, String license,
//                                                     String label, LongInterval commits, LongInterval contributors,
//                                                     LongInterval issues, LongInterval pulls, LongInterval branches,
//                                                     LongInterval releases, LongInterval stars, LongInterval watchers,
//                                                     LongInterval forks, DateInterval created, DateInterval committed,
//                                                     Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
//                                                     Boolean hasPulls, Boolean hasWiki, Boolean hasLicense) {
//        JPAQueryBuilder qb = constructQueryParameters(name, nameEquals, language, license, label, commits, contributors,
//                issues, pulls, branches, releases, stars, watchers, forks, created,
//                committed, excludeForks, onlyForks, hasIssues, hasPulls, hasWiki,
//                hasLicense);
//        qb.select("r", true);
//        qb.orderBy("r.name");
//        return entityManager.createQuery(qb.build(), GitRepo.class);
//    }

//    private JPAQueryBuilder constructQueryParameters(String name, Boolean nameEquals, String language, String license,
//                                                     String label, LongInterval commits, LongInterval contributors,
//                                                     LongInterval issues, LongInterval pulls, LongInterval branches,
//                                                     LongInterval releases, LongInterval stars, LongInterval watchers,
//                                                     LongInterval forks, DateInterval created, DateInterval committed,
//                                                     Boolean excludeForks, Boolean onlyForks, Boolean hasIssues,
//                                                     Boolean hasPulls, Boolean hasWiki, Boolean hasLicense) {
//        JPAQueryBuilder qb = new JPAQueryBuilder();
//        qb.from("GitRepo", "r");
//        if (StringUtils.isNoneBlank(label)) {
//            qb.join("GitRepoLabel", "rl", "r.id = rl.repo.id", Join.LEFT);
//        }
//
//        if (StringUtils.isNotBlank(name)) {
//            if (nameEquals) {
//                qb.where("r.name = (:name)", Operator.AND);
//            } else {
//                qb.where("lower(r.name) like lower(:name)", Operator.AND);
//            }
//        }
//
//        if (StringUtils.isNoneBlank(language)) {
//            qb.where("r.mainLanguage = (:language)", Operator.AND);
//        }
//
//        if (StringUtils.isNoneBlank(license)){
//            qb.where("r.license = (:license)", Operator.AND);
//        }
//
//        if (StringUtils.isNoneBlank(label)){
//            qb.where("lower(rl.label) = lower(:label)", Operator.AND);
//        }
//
//        if (commits.isLowerBound()){
//            qb.where("r.commits >= (:commitsMin)",Operator.AND);
//        } else if (commits.isUpperBound()){
//            qb.where("r.commits <= (:commitsMax)",Operator.AND);
//        } else if (commits.isBound()){
//            qb.where("r.commits between (:commitsMin) and (:commitsMax)",Operator.AND);
//        }
//
//        if (contributors.isLowerBound()){
//            qb.where("r.contributors >= (:contributorsMin)",Operator.AND);
//        } else if (contributors.isUpperBound()){
//            qb.where("r.contributors <= (:contributorsMax)",Operator.AND);
//        } else if (contributors.isBound()){
//            qb.where("r.contributors between (:contributorsMin) and (:contributorsMax)",Operator.AND);
//        }
//
//        if (issues.isLowerBound()){
//            qb.where("r.totalIssues >= (:issuesMin)",Operator.AND);
//        } else if (issues.isUpperBound()){
//            qb.where("r.totalIssues <= (:issuesMax)",Operator.AND);
//        } else if (issues.isBound()){
//            qb.where("r.totalIssues between (:issuesMin) and (:issuesMax)",Operator.AND);
//        }
//
//        if (pulls.isLowerBound()){
//            qb.where("r.totalPullRequests >= (:pullsMin)",Operator.AND);
//        } else if (pulls.isUpperBound()){
//            qb.where("r.totalPullRequests <= (:pullsMax)",Operator.AND);
//        } else if (pulls.isBound()){
//            qb.where("r.totalPullRequests between (:pullsMin) and (:pullsMax)",Operator.AND);
//        }
//
//        if (branches.isLowerBound()){
//            qb.where("r.branches >= (:branchesMin)",Operator.AND);
//        } else if (branches.isUpperBound()){
//            qb.where("r.branches <= (:branchesMax)",Operator.AND);
//        } else if (branches.isBound()){
//            qb.where("r.branches between (:branchesMin) and (:branchesMax)",Operator.AND);
//        }
//
//        if (releases.isLowerBound()){
//            qb.where("r.releases >= (:releasesMin)",Operator.AND);
//        } else if (releases.isUpperBound()){
//            qb.where("r.releases <= (:releasesMax)",Operator.AND);
//        } else if (releases.isBound()){
//            qb.where("r.releases between (:releasesMin) and (:releasesMax)",Operator.AND);
//        }
//
//        if (stars.isLowerBound()){
//            qb.where("r.stargazers >= (:starsMin)",Operator.AND);
//        } else if (stars.isUpperBound()){
//            qb.where("r.stargazers <= (:starsMax)",Operator.AND);
//        } else if (stars.isBound()){
//            qb.where("r.stargazers between (:starsMin) and (:starsMax)",Operator.AND);
//        }
//
//        if (watchers.isLowerBound()){
//            qb.where("r.watchers >= (:watchersMin)",Operator.AND);
//        } else if (watchers.isUpperBound()){
//            qb.where("r.watchers <= (:watchersMax)",Operator.AND);
//        } else if (watchers.isBound()){
//            qb.where("r.watchers between (:watchersMin) and (:watchersMax)",Operator.AND);
//        }
//
//        if (forks.isLowerBound()){
//            qb.where("r.forks >= (:forksMin)",Operator.AND);
//        } else if (forks.isUpperBound()){
//            qb.where("r.forks <= (:forksMax)",Operator.AND);
//        } else if (forks.isBound()){
//            qb.where("r.forks between (:forksMin) and (:forksMax)",Operator.AND);
//        }
//
//        if (created.isLowerBound()){
//            qb.where("date(r.createdAt) >= (:createdMin)",Operator.AND);
//        } else if (created.isUpperBound()){
//            qb.where("date(r.createdAt) <= (:createdMax)",Operator.AND);
//        } else if (created.isBound()){
//            qb.where("date(r.createdAt) between (:createdMin) and (:createdMax)",Operator.AND);
//        }
//
//        if (committed.isLowerBound()){
//            qb.where("date(r.pushedAt) >= (:pushedMin)",Operator.AND);
//        } else if (committed.isUpperBound()){
//            qb.where("date(r.pushedAt) <= (:pushedMax)",Operator.AND);
//        } else if (committed.isBound()){
//            qb.where("date(r.pushedAt) between (:pushedMin) and (:pushedMax)",Operator.AND);
//        }
//
//        if (excludeForks) {
//            qb.where("r.isFork = false", Operator.AND);
//        } else if (onlyForks) {
//            qb.where("r.isFork = true", Operator.AND);
//        }
//
//        if (hasIssues){
//            qb.where("r.openIssues > 0",Operator.AND);
//        }
//
//        if (hasPulls){
//            qb.where("r.openPullRequests > 0",Operator.AND);
//        }
//
//        if (hasWiki){
//            qb.where("r.hasWiki is true",Operator.AND);
//        }
//
//        if (hasLicense){
//            qb.where("r.license is not null",Operator.AND);
//        }
//
//        return qb;
//    }
//
//    private Map<String,Object> constructParameterMap(String name, Boolean nameEquals, String language, String license,
//                                                     String label, LongInterval commits, LongInterval contributors,
//                                                     LongInterval issues, LongInterval pulls, LongInterval branches,
//                                                     LongInterval releases, LongInterval stars, LongInterval watchers,
//                                                     LongInterval forks, DateInterval created, DateInterval committed)
//    {
//        Map<String,Object> parameters = new HashMap<>();
//
//        if (StringUtils.isNotBlank(name)){
//            if (nameEquals){
//                parameters.put("name", name);
//            } else {
//                parameters.put("name", "%"+name+"%");
//            }
//        }
//
//        if (StringUtils.isNotBlank(language)){
//            parameters.put("language", language);
//        }
//
//        if (StringUtils.isNotBlank(license)){
//            parameters.put("license", license);
//        }
//
//        if (StringUtils.isNotBlank(label)){
//            parameters.put("label",label);
//        }
//
//        if (commits.isLowerBound()){
//            parameters.put("commitsMin",commits.getStart());
//        } else if (commits.isUpperBound()){
//            parameters.put("commitsMax",commits.getEnd());
//        } else if (commits.isBound()){
//            parameters.put("commitsMin",commits.getStart());
//            parameters.put("commitsMax",commits.getEnd());
//        }
//
//        if (contributors.isLowerBound()){
//            parameters.put("contributorsMin",contributors.getStart());
//        } else if (contributors.isUpperBound()){
//            parameters.put("contributorsMax",contributors.getEnd());
//        } else if (contributors.isBound()){
//            parameters.put("contributorsMin",contributors.getStart());
//            parameters.put("contributorsMax",contributors.getEnd());
//        }
//
//        if (issues.isLowerBound()){
//            parameters.put("issuesMin",issues.getStart());
//        } else if (issues.isUpperBound()){
//            parameters.put("issuesMax",issues.getEnd());
//        } else if (issues.isBound()){
//            parameters.put("issuesMin",issues.getStart());
//            parameters.put("issuesMax",issues.getEnd());
//        }
//
//        if (pulls.isLowerBound()){
//            parameters.put("pullsMin",pulls.getStart());
//        } else if (pulls.isUpperBound()){
//            parameters.put("pullsMax",pulls.getEnd());
//        } else if (pulls.isBound()){
//            parameters.put("pullsMin",pulls.getStart());
//            parameters.put("pullsMax",pulls.getEnd());
//        }
//
//        if (branches.isLowerBound()){
//            parameters.put("branchesMin",branches.getStart());
//        } else if (branches.isUpperBound()){
//            parameters.put("branchesMax",branches.getEnd());
//        } else if (branches.isBound()){
//            parameters.put("branchesMin",branches.getStart());
//            parameters.put("branchesMax",branches.getEnd());
//        }
//
//        if (releases.isLowerBound()){
//            parameters.put("releasesMin",releases.getStart());
//        } else if (releases.isUpperBound()){
//            parameters.put("releasesMax",releases.getEnd());
//        } else if (releases.isBound()){
//            parameters.put("releasesMin",releases.getStart());
//            parameters.put("releasesMax",releases.getEnd());
//        }
//
//        if (stars.isLowerBound()){
//            parameters.put("starsMin",stars.getStart());
//        } else if (stars.isUpperBound()){
//            parameters.put("starsMax",stars.getEnd());
//        } else if (stars.isBound()){
//            parameters.put("starsMin",stars.getStart());
//            parameters.put("starsMax",stars.getEnd());
//        }
//
//        if (watchers.isLowerBound()){
//            parameters.put("watchersMin",watchers.getStart());
//        } else if (watchers.isUpperBound()){
//            parameters.put("watchersMax",watchers.getEnd());
//        } else if (watchers.isBound()){
//            parameters.put("watchersMin",watchers.getStart());
//            parameters.put("watchersMax",watchers.getEnd());
//        }
//
//        if (forks.isLowerBound()){
//            parameters.put("forksMin",forks.getStart());
//        } else if (forks.isUpperBound()){
//            parameters.put("forksMax",forks.getEnd());
//        } else if (forks.isBound()){
//            parameters.put("forksMin",forks.getStart());
//            parameters.put("forksMax",forks.getEnd());
//        }
//
//        if (created.isLowerBound()){
//            parameters.put("createdMin",created.getStart());
//        } else if (created.isUpperBound()){
//            parameters.put("createdMax",created.getEnd());
//        } else if (created.isBound()){
//            parameters.put("createdMin",created.getStart());
//            parameters.put("createdMax",created.getEnd());
//        }
//
//        if (committed.isLowerBound()){
//            parameters.put("pushedMin", committed.getStart());
//        } else if (committed.isUpperBound()) {
//            parameters.put("pushedMax", committed.getEnd());
//        } else if (committed.isBound()) {
//            parameters.put("pushedMin", committed.getStart());
//            parameters.put("pushedMax", committed.getEnd());
//        }
//
//        return parameters;
//    }


    /**
     * Warning: This method is supposed to improve efficiency of JPA approach. It contains "column-names".
     * @author Emad
     */
    public List<GitRepoDto> advancedSearch_emad(String name, Boolean nameEquals, String language, String license, String label,
                                                                    LongInterval commits, LongInterval contributors, LongInterval issues,
                                                                    LongInterval pulls, LongInterval branches, LongInterval releases,
                                                                    LongInterval stars, LongInterval watchers, LongInterval forks,
                                                                    DateInterval created, DateInterval committed, Boolean excludeForks,
                                                                    Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                                                    Boolean hasLicense, Pageable pageable) {

        String query_parameters = constructAdvancedSearchParameters_emad(name, nameEquals, language, license, label, commits, contributors, issues,
                pulls, branches, releases, stars, watchers, forks, created, committed,
                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense, true);

        StringBuilder query_str = new StringBuilder();
        query_str.append("SELECT r.*, rlabel.labels, rlang.langs\n");
        query_str.append(query_parameters);
        query_str.append("\nORDER BY r.name ASC ");
        if (pageable != null) {
            query_str.append(String.format("\nLIMIT %d OFFSET %d ", pageable.getPageSize(), pageable.getPageSize() * pageable.getPageNumber()));
        }

        List<Object[]> resultList = entityManager.createNativeQuery(query_str.toString()).getResultList();

        List<GitRepoDto> res = new ArrayList<>();
        for (var row : resultList) {

            List<GitRepoLabelDto> labels = new ArrayList<>();
            if(row[27]!=null)
                for(String l: ((String) row[27]).split(","))
                    labels.add(GitRepoLabelDto.builder().label(l).build());

            List<GitRepoLanguageDto> languages = new ArrayList<>();
            if(row[28]!=null)
                for(String l: ((String) row[28]).split(",")) {
                    String[] split = l.split("~");
                    languages.add(GitRepoLanguageDto.builder().language(split[0]).sizeOfCode(Long.valueOf(split[1])).build());
                }

            GitRepoDto repo = GitRepoDto.builder()
                    .id(row[0]!=null?((BigInteger) row[0]).longValue():null)
                    .name((String) row[1])
                    .isFork((Boolean) row[2])
                    .commits(row[3]!=null?((BigInteger) row[3]).longValue():null)
                    .branches(row[4]!=null?((BigInteger) row[4]).longValue():null)
                    .defaultBranch((String) row[5])
                    .releases(row[6]!=null?((BigInteger) row[6]).longValue():null)
                    .contributors(row[7]!=null?((BigInteger) row[7]).longValue():null)
                    .license((String) row[8])
                    .watchers(row[9]!=null?((BigInteger) row[9]).longValue():null)
                    .stargazers(row[10]!=null?((BigInteger) row[10]).longValue():null)
                    .forks(row[11]!=null?((BigInteger) row[11]).longValue():null)
                    .size(row[12]!=null?((BigInteger) row[12]).longValue():null)
                    .createdAt((Date) row[13])
                    .pushedAt((Date) row[14])
                    .updatedAt((Date) row[15])
                    .homepage((String) row[16])
                    .mainLanguage((String) row[17])
                    .totalIssues(row[18]!=null?((BigInteger) row[18]).longValue():null)
                    .openIssues(row[19]!=null?((BigInteger) row[19]).longValue():null)
                    .totalPullRequests(row[20]!=null?((BigInteger) row[20]).longValue():null)
                    .openPullRequests(row[21]!=null?((BigInteger) row[21]).longValue():null)
                    .lastCommit((Date) row[22])
                    .lastCommitSHA((String) row[23])
                    .hasWiki((Boolean) row[24])
                    .isArchived((Boolean) row[25])
                    .labels(labels)
                    .languages(languages)
                    .build();
            res.add(repo);
        }
        return res;
    }


    public String constructAdvancedSearchParameters_emad(String name, Boolean nameEquals, String language, String license, String label,
                                             LongInterval commits, LongInterval contributors, LongInterval issues,
                                             LongInterval pulls, LongInterval branches, LongInterval releases,
                                             LongInterval stars, LongInterval watchers, LongInterval forks,
                                             DateInterval created, DateInterval committed, Boolean excludeForks,
                                             Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                             Boolean hasLicense, Boolean shouldFetchLabelsAndLanguagesInfo)
    {
        StringBuilder query = new StringBuilder();

        if(shouldFetchLabelsAndLanguagesInfo) {
            String label_where_clause = "";
            if (StringUtils.isNoneBlank(label)) {
                label_where_clause = String.format("WHERE repo_id IN (SELECT repo_id FROM repo_label WHERE LOWER(repo_label_name)=LOWER('%s'))\n", label);
            }
            query.append("FROM repo r left join ( SELECT repo_id, GROUP_CONCAT(repo_label_name) AS labels \n" +
                    "                        FROM repo_label \n" + label_where_clause +
                    "                        GROUP BY repo_id) rlabel ON r.id = rlabel.repo_id\n" +
                    "            left join ( SELECT repo_id, GROUP_CONCAT(repo_language_name,'~',size_of_code) AS langs \n" +
                    "                        FROM repo_language \n" +
                    "                        GROUP BY repo_id) rlang ON r.id = rlang.repo_id\n" +
                    "WHERE 1=1 ");
            if (StringUtils.isNoneBlank(label)) {
                query.append("AND rlabel.labels IS NOT NULL ");
            }
        }
        else
        {
            String label_where_clause = "";
            if (StringUtils.isNoneBlank(label)) {
                label_where_clause = String.format("AND r.id IN (SELECT repo_id FROM repo_label WHERE repo_label_name='%s') ", label);
            }
            query.append("FROM repo r \n WHERE 1=1 "+ label_where_clause);
        }


        if (StringUtils.isNotBlank(name)) {
            if (nameEquals) {
                query.append(String.format("AND r.name = lower('%s') ", name));
            } else {
                query.append(String.format("AND r.name LIKE lower('%%%s%%') ", name));
            }
        }

        if (StringUtils.isNoneBlank(language)) {
            query.append(String.format("AND r.main_Language = '%s' ", language));
        }

        if (StringUtils.isNoneBlank(license)) {
            query.append(String.format("AND r.license = '%s' ", license));
        }

        if (commits.isLowerBound()) {
            query.append(String.format("AND r.commits >= %d ", commits.getStart()));
//            qb.where("r.commits >= (:commitsMin)",Operator.AND);
        } else if (commits.isUpperBound()) {
            query.append(String.format("AND r.commits <= %d ", commits.getEnd()));
//            qb.where("r.commits <= (:commitsMax)",Operator.AND);
        } else if (commits.isBound()) {
            query.append(String.format("AND r.commits BETWEEN %d AND %d ", commits.getStart(), commits.getEnd()));
//            qb.where("r.commits between (:commitsMin) and (:commitsMax)",Operator.AND);
        }

        if (contributors.isLowerBound()) {
            query.append(String.format("AND r.contributors >= %d ", contributors.getStart()));
//            qb.where("r.contributors >= (:contributorsMin)",Operator.AND);
        } else if (contributors.isUpperBound()) {
            query.append(String.format("AND r.contributors <= %d ", contributors.getEnd()));
//            qb.where("r.contributors <= (:contributorsMax)",Operator.AND);
        } else if (contributors.isBound()) {
            query.append(String.format("AND r.contributors BETWEEN %d AND %d ", contributors.getStart(), contributors.getEnd()));
//            qb.where("r.contributors between (:contributorsMin) and (:contributorsMax)",Operator.AND);
        }

        if (issues.isLowerBound()) {
            query.append(String.format("AND r.total_issues >= %d ", issues.getStart()));
//            qb.where("r.totalIssues >= (:issuesMin)",Operator.AND);
        } else if (issues.isUpperBound()) {
            query.append(String.format("AND r.total_issues <= %d ", issues.getEnd()));
//            qb.where("r.totalIssues <= (:issuesMax)",Operator.AND);
        } else if (issues.isBound()) {
            query.append(String.format("AND r.total_issues BETWEEN %d AND %d ", issues.getStart(), issues.getEnd()));
//            qb.where("r.totalIssues between (:issuesMin) and (:issuesMax)",Operator.AND);
        }

        if (pulls.isLowerBound()) {
            query.append(String.format("AND r.total_pull_requests >= %d ", pulls.getStart()));
//            qb.where("r.totalPullRequests >= (:pullsMin)",Operator.AND);
        } else if (pulls.isUpperBound()) {
            query.append(String.format("AND r.total_pull_requests <= %d ", pulls.getEnd()));
//            qb.where("r.totalPullRequests <= (:pullsMax)",Operator.AND);
        } else if (pulls.isBound()) {
            query.append(String.format("AND r.total_pull_requests BETWEEN %d AND %d ", pulls.getStart(), pulls.getEnd()));
//            qb.where("r.totalPullRequests between (:pullsMin) and (:pullsMax)",Operator.AND);
        }

        if (branches.isLowerBound()) {
            query.append(String.format("AND r.branches >= %d ", branches.getStart()));
//            qb.where("r.branches >= (:branchesMin)",Operator.AND);
        } else if (branches.isUpperBound()) {
            query.append(String.format("AND r.branches <= %d ", branches.getEnd()));
//            qb.where("r.branches <= (:branchesMax)",Operator.AND);
        } else if (branches.isBound()) {
            query.append(String.format("AND r.branches BETWEEN %d AND %d ", branches.getStart(), branches.getEnd()));
//            qb.where("r.branches between (:branchesMin) and (:branchesMax)",Operator.AND);
        }

        if (releases.isLowerBound()) {
            query.append(String.format("AND r.releases >= %d ", releases.getStart()));
//            qb.where("r.releases >= (:releasesMin)",Operator.AND);
        } else if (releases.isUpperBound()) {
            query.append(String.format("AND r.releases <= %d ", releases.getEnd()));
//            qb.where("r.releases <= (:releasesMax)",Operator.AND);
        } else if (releases.isBound()) {
            query.append(String.format("AND r.releases BETWEEN %d AND %d ", releases.getStart(), releases.getEnd()));
//            qb.where("r.releases between (:releasesMin) and (:releasesMax)",Operator.AND);
        }

        if (stars.isLowerBound()) {
            query.append(String.format("AND r.stargazers >= %d ", stars.getStart()));
//            qb.where("r.stargazers >= (:starsMin)",Operator.AND);
        } else if (stars.isUpperBound()) {
            query.append(String.format("AND r.stargazers <= %d ", stars.getEnd()));
//            qb.where("r.stargazers <= (:starsMax)",Operator.AND);
        } else if (stars.isBound()) {
            query.append(String.format("AND r.stargazers BETWEEN %d AND %d ", stars.getStart(), stars.getEnd()));
//            qb.where("r.stargazers between (:starsMin) and (:starsMax)",Operator.AND);
        }

        if (watchers.isLowerBound()) {
            query.append(String.format("AND r.watchers >= %d ", watchers.getStart()));
//            qb.where("r.watchers >= (:watchersMin)",Operator.AND);
        } else if (watchers.isUpperBound()) {
            query.append(String.format("AND r.watchers <= %d ", watchers.getEnd()));
//            qb.where("r.watchers <= (:watchersMax)",Operator.AND);
        } else if (watchers.isBound()) {
            query.append(String.format("AND r.watchers BETWEEN %d AND %d ", watchers.getStart(), watchers.getEnd()));
//            qb.where("r.watchers between (:watchersMin) and (:watchersMax)",Operator.AND);
        }

        if (forks.isLowerBound()) {
            query.append(String.format("AND r.forks >= %d ", forks.getStart()));
//            qb.where("r.forks >= (:forksMin)",Operator.AND);
        } else if (forks.isUpperBound()) {
            query.append(String.format("AND r.forks <= %d ", forks.getEnd()));
//            qb.where("r.forks <= (:forksMax)",Operator.AND);
        } else if (forks.isBound()) {
            query.append(String.format("AND r.forks BETWEEN %d AND %d ", forks.getStart(), forks.getEnd()));
//            qb.where("r.forks between (:forksMin) and (:forksMax)",Operator.AND);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date createdMax = null;
        if (created.isUpperBound() || created.isBound())
            createdMax = Date.from(created.getEnd().toInstant().plus(1, ChronoUnit.DAYS).minusSeconds(1));

        if (created.isLowerBound()) {
            query.append(String.format("AND r.created_at >= '%s' ", dateFormat.format(created.getStart())));
//            qb.where("date(r.createdAt) >= (:createdMin)",Operator.AND);
        } else if (created.isUpperBound()) {
            query.append(String.format("AND r.created_at <= '%s' ", dateFormat.format(createdMax)));
//            qb.where("date(r.createdAt) <= (:createdMax)",Operator.AND);
        } else if (created.isBound()) {
            query.append(String.format("AND r.created_at BETWEEN '%s' AND '%s' ", dateFormat.format(created.getStart()), dateFormat.format(createdMax)));
//            qb.where("date(r.createdAt) between (:createdMin) and (:createdMax)",Operator.AND);
        }

        Date committedMax = null;
        if (committed.isUpperBound() || committed.isBound())
            committedMax = Date.from(committed.getEnd().toInstant().plus(1, ChronoUnit.DAYS).minusSeconds(1));

        if (committed.isLowerBound()) {
            query.append(String.format("AND r.pushed_at >= '%s' ", dateFormat.format(committed.getStart())));
//            qb.where("date(r.pushedAt) >= (:pushedMin)",Operator.AND);
        } else if (committed.isUpperBound()) {
            query.append(String.format("AND r.pushed_at <= '%s' ", dateFormat.format(committedMax)));
//            qb.where("date(r.pushedAt) <= (:pushedMax)",Operator.AND);
        } else if (committed.isBound()) {
            query.append(String.format("AND r.pushed_at BETWEEN '%s' AND '%s' ", dateFormat.format(committed.getStart()), dateFormat.format(committedMax)));
//            qb.where("date(r.pushedAt) between (:pushedMin) and (:pushedMax)",Operator.AND);
        }

        if (excludeForks) {
            query.append("AND r.is_fork_project = FALSE ");
//            qb.where("r.isFork = false",Operator.AND);
        } else if (onlyForks) {
            query.append("AND r.is_fork_project = TRUE ");
//            qb.where("r.isFork = true",Operator.AND);
        }

        if (hasIssues) {
            query.append("AND r.open_issues > 0 ");
//            qb.where("r.openIssues > 0",Operator.AND);
        }

        if (hasPulls) {
            query.append("AND r.open_pull_requests > 0 ");
//            qb.where("r.openPullRequests > 0",Operator.AND);
        }

        if (hasWiki) {
            query.append("AND r.has_wiki = TRUE ");
//            qb.where("r.hasWiki is true",Operator.AND);
        }

        if (hasLicense) {
            query.append("AND r.license IS NOT NULL ");
//            qb.where("r.license is not null",Operator.AND);
        }

        return query.toString();
    }

    public Long countAdvancedSearch_emad(String name, Boolean nameEquals, String language, String license, String label,
                                                     LongInterval commits, LongInterval contributors, LongInterval issues,
                                                     LongInterval pulls, LongInterval branches, LongInterval releases,
                                                     LongInterval stars, LongInterval watchers, LongInterval forks,
                                                     DateInterval created, DateInterval committed, Boolean excludeForks,
                                                     Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                                     Boolean hasLicense)
    {
        String query_parameters = constructAdvancedSearchParameters_emad(name, nameEquals, language, license, label, commits, contributors, issues,
                pulls, branches, releases, stars, watchers, forks, created, committed,
                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense, false);

        String query_str = "SELECT COUNT(*)\n" + query_parameters;

        Object resultList = entityManager.createNativeQuery(query_str).getSingleResult();
        Long count = ((BigInteger) resultList).longValue();
        return count;
    }

}
