package usi.si.seart.gseapp.repository.impl;

import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.dto.GitRepoLabelDto;
import usi.si.seart.gseapp.dto.GitRepoLanguageDto;
import usi.si.seart.gseapp.repository.GitRepoRepositoryCustom;
import usi.si.seart.gseapp.util.interval.DateInterval;
import usi.si.seart.gseapp.util.interval.LongInterval;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoRepositoryCustomImpl implements GitRepoRepositoryCustom {

    EntityManager entityManager;

    /**
     * The method is implemented using NativeQuery (not JPL/Hibernate way) and it contains "column-names".
     * Why? In order to perform single query (to improve efficiency) we had to use GROUP_CONCAT and other MySQL specific
     * functions, but with Hibernate this was not easily achievable.
     * @author Emad
     */
    public List<GitRepoDto> AdvancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                           LongInterval commits, LongInterval contributors, LongInterval issues,
                                           LongInterval pulls, LongInterval branches, LongInterval releases,
                                           LongInterval stars, LongInterval watchers, LongInterval forks,
                                           DateInterval created, DateInterval committed, Boolean excludeForks,
                                           Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                           Boolean hasLicense, Pageable pageable) {

        String query_parameters = ConstructQueryTemplate(name, nameEquals, language, license, label, commits, contributors, issues,
                pulls, branches, releases, stars, watchers, forks, created, committed,
                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense, true);
        Map<String, Object> parameters = ConstructParameterMap(name, nameEquals, language, license, label, commits,  contributors, issues, pulls, branches, releases, stars, watchers, forks, created, committed);

        String query_str = "SELECT r.*, rlabel.labels, rlang.langs\n" +
                query_parameters +
                "\nORDER BY r.name ASC ";

        Query nativeQuery = entityManager.createNativeQuery(query_str);
        parameters.keySet().forEach(k -> nativeQuery.setParameter(k, parameters.get(k)));
        if (pageable != null) {
            nativeQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
            nativeQuery.setMaxResults(pageable.getPageSize());
        }

        List<Object[]> resultList = nativeQuery.getResultList();
        List<GitRepoDto> res = new ArrayList<>();
        for (var row : resultList) {

            String repoFullname = (String) row[1];

            List<GitRepoLabelDto> labels = new ArrayList<>();
            if(row[27]!=null)
                for(String l: ((String) row[27]).split(","))
                    labels.add(GitRepoLabelDto.builder().label(l).build());

            List<GitRepoLanguageDto> languages = new ArrayList<>();
            if(row[28]!=null)
                for(String l: ((String) row[28]).split(",")) {
                    String[] split = l.split("~");
                    if(split.length!=2)
                    {
                        System.err.printf("Ignoring ill-formatted list of languages for %s (see next line). This is most probably because `group_concat_max_len` is not set to a high value.\n", repoFullname);
                        System.err.println("> "+row[28]);
                        languages.clear();
                        break;
                    }
                    languages.add(GitRepoLanguageDto.builder().language(split[0]).sizeOfCode(Long.valueOf(split[1])).build());
                }

            GitRepoDto repo = GitRepoDto.builder()
                    .id(row[0]!=null?((BigInteger) row[0]).longValue():null)
                    .name(repoFullname)
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

    public Long AdvancedSearchCount(String name, Boolean nameEquals, String language, String license, String label,
                                    LongInterval commits, LongInterval contributors, LongInterval issues,
                                    LongInterval pulls, LongInterval branches, LongInterval releases,
                                    LongInterval stars, LongInterval watchers, LongInterval forks,
                                    DateInterval created, DateInterval committed, Boolean excludeForks,
                                    Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                    Boolean hasLicense)
    {
        String query_parameters = ConstructQueryTemplate(name, nameEquals, language, license, label, commits, contributors, issues,
                pulls, branches, releases, stars, watchers, forks, created, committed,
                excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense, false);

        String query_str = "SELECT COUNT(*)\n" + query_parameters;

        Query nativeQuery = entityManager.createNativeQuery(query_str);
        Map<String, Object> parameters = ConstructParameterMap(name, nameEquals, language, license, label, commits,  contributors, issues, pulls, branches, releases, stars, watchers, forks, created, committed);
        parameters.keySet().forEach(k -> nativeQuery.setParameter(k, parameters.get(k)));
        Object resultList = nativeQuery.getSingleResult();
        Long count = ((BigInteger) resultList).longValue();
        return count;
    }




    public String ConstructQueryTemplate(String name, Boolean nameEquals, String language, String license, String label,
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
                label_where_clause = "WHERE repo_id IN (SELECT repo_id FROM repo_label WHERE LOWER(repo_label_name)=LOWER(:label))\n";
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
                label_where_clause = "AND r.id IN (SELECT repo_id FROM repo_label WHERE repo_label_name=(:label)) ";
            }
            query.append("FROM repo r \n WHERE 1=1 "+ label_where_clause);
        }


        if (StringUtils.isNotBlank(name)) {
            if (nameEquals) {
                query.append("AND lower(r.name) = lower(:name) ");
            } else {
                query.append("AND r.name LIKE lower(:name) ");
            }
        }

        if (StringUtils.isNoneBlank(language)) {
            query.append("AND r.main_Language = (:language) ");
        }

        if (StringUtils.isNoneBlank(license)) {
            query.append("AND r.license = (:license) ");
        }

        if (commits.isLowerBound()) {
            query.append("AND r.commits >= (:commitsMin) ");
        } else if (commits.isUpperBound()) {
            query.append("AND r.commits <= (:commitsMax) ");
        } else if (commits.isBound()) {
            query.append("AND r.commits BETWEEN (:commitsMin) and (:commitsMax) ");
        }

        if (contributors.isLowerBound()) {
            query.append("AND r.contributors >= (:contributorsMin) ");
        } else if (contributors.isUpperBound()) {
            query.append("AND r.contributors <= (:contributorsMax) ");
        } else if (contributors.isBound()) {
            query.append("AND r.contributors BETWEEN (:contributorsMin) AND (:contributorsMax) ");
        }

        if (issues.isLowerBound()) {
            query.append("AND r.total_issues >= (:issuesMin) ");
        } else if (issues.isUpperBound()) {
            query.append("AND r.total_issues <= (:issuesMax) ");
        } else if (issues.isBound()) {
            query.append("AND r.total_issues BETWEEN (:issuesMin) AND (:issuesMax) ");
        }

        if (pulls.isLowerBound()) {
            query.append("AND r.total_pull_requests >= (:pullsMin) ");
        } else if (pulls.isUpperBound()) {
            query.append("AND r.total_pull_requests <= (:pullsMax) ");
        } else if (pulls.isBound()) {
            query.append("AND r.total_pull_requests BETWEEN (:pullsMin) AND (:pullsMax) ");
        }

        if (branches.isLowerBound()) {
            query.append("AND r.branches >=(:branchesMin) ");
        } else if (branches.isUpperBound()) {
            query.append("AND r.branches <= (:branchesMax) ");
        } else if (branches.isBound()) {
            query.append("AND r.branches BETWEEN (:branchesMin) AND (:branchesMax) ");
        }

        if (releases.isLowerBound()) {
            query.append("AND r.releases >= (:releasesMin) ");
        } else if (releases.isUpperBound()) {
            query.append("AND r.releases <= (:releasesMax) ");
        } else if (releases.isBound()) {
            query.append("AND r.releases BETWEEN (:releasesMin) AND (:releasesMax) ");
        }

        if (stars.isLowerBound()) {
            query.append("AND r.stargazers >= (:starsMin) ");
        } else if (stars.isUpperBound()) {
            query.append("AND r.stargazers <= (:starsMax) ");
        } else if (stars.isBound()) {
            query.append("AND r.stargazers BETWEEN (:starsMin) AND (:starsMax) ");
        }

        if (watchers.isLowerBound()) {
            query.append("AND r.watchers >= (:watchersMin) ");
        } else if (watchers.isUpperBound()) {
            query.append("AND r.watchers <= (:watchersMax) ");
        } else if (watchers.isBound()) {
            query.append("AND r.watchers BETWEEN (:watchersMin) AND (:watchersMax) ");
        }

        if (forks.isLowerBound()) {
            query.append("AND r.forks >= (:forksMin) ");
        } else if (forks.isUpperBound()) {
            query.append("AND r.forks <= (:forksMax) ");
        } else if (forks.isBound()) {
            query.append("AND r.forks BETWEEN (:forksMin) AND (:forksMax) ");
        }


        if (created.isLowerBound()) {
            query.append("AND date(r.created_at) >= (:createdMin) ");
        } else if (created.isUpperBound()) {
            query.append("AND date(r.created_at) <= (:createdMax) ");
        } else if (created.isBound()) {
            query.append("AND date(r.created_at) BETWEEN (:createdMin) AND (:createdMax) ");
        }



        if (committed.isLowerBound()) {
            query.append("AND date(r.pushed_at) >= (:pushedMin) ");
        } else if (committed.isUpperBound()) {
            query.append("AND date(r.pushed_at) <= (:pushedMax) ");
        } else if (committed.isBound()) {
            query.append("AND date(r.pushed_at) BETWEEN (:pushedMin) AND (:pushedMax) ");
        }

        if (excludeForks) {
            query.append("AND r.is_fork_project = FALSE ");
        } else if (onlyForks) {
            query.append("AND r.is_fork_project = TRUE ");
        }

        if (hasIssues) {
            query.append("AND r.open_issues > 0 ");
        }

        if (hasPulls) {
            query.append("AND r.open_pull_requests > 0 ");
        }

        if (hasWiki) {
            query.append("AND r.has_wiki = TRUE ");
        }

        if (hasLicense) {
            query.append("AND r.license IS NOT NULL ");
        }

        return query.toString();
    }




    private Map<String,Object> ConstructParameterMap(String name, Boolean nameEquals, String language, String license,
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


        Date createdMax = null;
        if (created.isUpperBound() || created.isBound())
            createdMax = Date.from(created.getEnd().toInstant().plus(1, ChronoUnit.DAYS).minusSeconds(1));

        if (created.isLowerBound()){
            parameters.put("createdMin",created.getStart());
        } else if (created.isUpperBound()){
            parameters.put("createdMax",createdMax);
        } else if (created.isBound()){
            parameters.put("createdMin",created.getStart());
            parameters.put("createdMax",createdMax);
        }


        Date committedMax = null;
        if (committed.isUpperBound() || committed.isBound())
            committedMax = Date.from(committed.getEnd().toInstant().plus(1, ChronoUnit.DAYS).minusSeconds(1));

        if (committed.isLowerBound()){
            parameters.put("pushedMin", committed.getStart());
        } else if (committed.isUpperBound()) {
            parameters.put("pushedMax", committedMax);
        } else if (committed.isBound()) {
            parameters.put("pushedMin", committed.getStart());
            parameters.put("pushedMax", committedMax);
        }

        return parameters;
    }

}
