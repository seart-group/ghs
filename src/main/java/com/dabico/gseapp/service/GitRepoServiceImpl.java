package com.dabico.gseapp.service;

import com.dabico.gseapp.controller.GitRepoController;
import com.dabico.gseapp.converter.GitRepoConverter;
import com.dabico.gseapp.dto.*;
import com.dabico.gseapp.model.GitRepo;
import com.dabico.gseapp.model.GitRepoLabel;
import com.dabico.gseapp.model.GitRepoLanguage;
import com.dabico.gseapp.repository.GitRepoLabelRepository;
import com.dabico.gseapp.repository.GitRepoLanguageRepository;
import com.dabico.gseapp.repository.GitRepoRepository;
import com.dabico.gseapp.repository.GitRepoRepositoryCustom;
import com.dabico.gseapp.util.interval.DateInterval;
import com.dabico.gseapp.util.interval.LongInterval;
import com.opencsv.CSVWriter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class GitRepoServiceImpl implements GitRepoService {
    GitRepoRepository gitRepoRepository;
    GitRepoRepositoryCustom gitRepoRepositoryCustom;
    GitRepoLabelRepository gitRepoLabelRepository;
    GitRepoLanguageRepository gitRepoLanguageRepository;
    GitRepoConverter gitRepoConverter;

    @Override
    public GitRepoDto getRepoById(Long repoId){
        GitRepo repo = gitRepoRepository.getOne(repoId);
        List<GitRepoLabel> labels = gitRepoLabelRepository.findRepoLabels(repoId);
        List<GitRepoLanguage> languages = gitRepoLanguageRepository.findRepoLanguages(repoId);
        GitRepoDto repoDto = gitRepoConverter.repoToRepoDto(repo);
        repoDto.setLabels(new ArrayList<>(gitRepoConverter.labelListToLabelDtoList(labels)));
        repoDto.setLanguages(new ArrayList<>(gitRepoConverter.languageListToLanguageDtoList(languages)));
        return repoDto;
    }

    @Override
    public List<GitRepoLabelDto> findRepoLabels(Long repoId){
        return gitRepoConverter.labelListToLabelDtoList(gitRepoLabelRepository.findRepoLabels(repoId));
    }

    @Override
    public List<GitRepoLanguageDto> findRepoLanguages(Long repoId){
        return gitRepoConverter.languageListToLanguageDtoList(gitRepoLanguageRepository.findRepoLanguages(repoId));
    }

    @Override
    public GitRepoDtoListPaginated advancedSearch(String name, Boolean nameEquals,String language, String license, String label,
                                                  Long commitsMin, Long commitsMax, Long contributorsMin, Long contributorsMax,
                                                  Long issuesMin, Long issuesMax, Long pullsMin, Long pullsMax, Long branchesMin,
                                                  Long branchesMax, Long releasesMin, Long releasesMax, Long starsMin,
                                                  Long starsMax, Long watchersMin, Long watchersMax, Long forksMin,
                                                  Long forksMax, Date createdMin, Date createdMax, Date committedMin,
                                                  Date committedMax, Boolean excludeForks,  Boolean onlyForks, Boolean hasIssues,
                                                  Boolean hasPulls, Boolean hasWiki, Boolean hasLicense, Integer page,
                                                  Integer pageSize){
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.ASC, "name");
        LongInterval commits      = new LongInterval(commitsMin,commitsMax);
        LongInterval contributors = new LongInterval(contributorsMin,contributorsMax);
        LongInterval issues       = new LongInterval(issuesMin,issuesMax);
        LongInterval pulls        = new LongInterval(pullsMin,pullsMax);
        LongInterval branches     = new LongInterval(branchesMin,branchesMax);
        LongInterval releases     = new LongInterval(releasesMin,releasesMax);
        LongInterval stars        = new LongInterval(starsMin,starsMax);
        LongInterval watchers     = new LongInterval(watchersMin,watchersMax);
        LongInterval forks        = new LongInterval(forksMin,forksMax);
        DateInterval created      = new DateInterval(createdMin,createdMax);
        DateInterval committed    = new DateInterval(committedMin,committedMax);
        List<GitRepo> repos = gitRepoRepositoryCustom.advancedSearch(name,nameEquals,language,license,label,commits,
                                                                     contributors,issues,pulls,branches,releases,
                                                                     stars,watchers,forks,created,committed,excludeForks,
                                                                     onlyForks,hasIssues,hasPulls,hasWiki,hasLicense,pageable);
        List<GitRepoDto> repoDtos = gitRepoConverter.repoListToRepoDtoList(repos);
        Long totalResults = gitRepoRepositoryCustom.countResults(name,nameEquals,language,license,label,commits,
                                                                 contributors,issues,pulls,branches,releases,
                                                                 stars,watchers,forks,created,committed,excludeForks,
                                                                 onlyForks,hasIssues,hasPulls,hasWiki,hasLicense);
        int lastPage = (int) (totalResults/pageSize);
        if (totalResults % pageSize == 0){ lastPage -= 1; }
        for (GitRepoDto repoDto : repoDtos){
            repoDto.setLabels(new ArrayList<>(findRepoLabels(repoDto.getId())));
            repoDto.setLanguages(new ArrayList<>(findRepoLanguages(repoDto.getId())));
        }

        GitRepoDtoListPaginated repoDtoListPaginated = GitRepoDtoListPaginated.builder().build();
        repoDtoListPaginated.setItems(repoDtos);
        repoDtoListPaginated.setTotalItems(totalResults);
        repoDtoListPaginated.setPage(page+1);
        if (page > 0){
            String prev = linkTo(methodOn(GitRepoController.class)
                    .searchRepos(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                                 contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,
                                 releasesMin,releasesMax,starsMin,starsMax,watchersMin,watchersMax,forksMin,forksMax,
                                 createdMin,createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,
                                 hasPulls,hasWiki,hasLicense,page - 1,pageSize)).toString().split("\\{")[0];
            repoDtoListPaginated.setPrev(prev);
            String first = linkTo(methodOn(GitRepoController.class)
                    .searchRepos(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                                 contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,
                                 releasesMin,releasesMax,starsMin,starsMax, watchersMin,watchersMax,forksMin,forksMax,
                                 createdMin,createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,
                                 hasPulls,hasWiki,hasLicense,0, pageSize)).toString().split("\\{")[0];
            repoDtoListPaginated.setFirst(first);
        }
        if (pageSize == repos.size()){
            String next = linkTo(methodOn(GitRepoController.class)
                    .searchRepos(name, nameEquals, language, license, label, commitsMin, commitsMax, contributorsMin, contributorsMax,
                                 issuesMin, issuesMax, pullsMin, pullsMax, branchesMin, branchesMax, releasesMin, releasesMax,
                                 starsMin, starsMax, watchersMin, watchersMax, forksMin, forksMax, createdMin, createdMax,
                                 committedMin, committedMax, excludeForks, onlyForks, hasIssues, hasPulls, hasWiki, hasLicense,
                                 page + 1, pageSize)).toString().split("\\{")[0];
            repoDtoListPaginated.setNext(next);
        }
        if (page < lastPage){
            String last = linkTo(methodOn(GitRepoController.class)
                    .searchRepos(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                                 contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,
                                 releasesMin,releasesMax,starsMin,starsMax,watchersMin,watchersMax,forksMin,forksMax,
                                 createdMin,createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,
                                 hasPulls,hasWiki,hasLicense,lastPage,pageSize)).toString().split("\\{")[0];
            repoDtoListPaginated.setLast(last);
        }
        if (totalResults > 0){
            String csvDownloadLink = linkTo(methodOn(GitRepoController.class)
                    .downloadCSV(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                            contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,releasesMin,
                            releasesMax,starsMin,starsMax,watchersMin,watchersMax,forksMin,forksMax,createdMin,
                            createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,
                            hasLicense)).toString().split("\\{")[0];
            repoDtoListPaginated.setCsvLink(csvDownloadLink);
            String jsonDownloadLink = linkTo(methodOn(GitRepoController.class)
                    .downloadJSON(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                            contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,releasesMin,
                            releasesMax,starsMin,starsMax,watchersMin,watchersMax,forksMin,forksMax,createdMin,
                            createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,
                            hasLicense)).toString().split("\\{")[0];
            repoDtoListPaginated.setJsonLink(jsonDownloadLink);
            String xmlDownloadLink = linkTo(methodOn(GitRepoController.class)
                    .downloadXML(name,nameEquals,language,license,label,commitsMin,commitsMax,contributorsMin,
                            contributorsMax,issuesMin,issuesMax,pullsMin,pullsMax,branchesMin,branchesMax,releasesMin,
                            releasesMax,starsMin,starsMax,watchersMin,watchersMax,forksMin,forksMax,createdMin,
                            createdMax,committedMin,committedMax,excludeForks,onlyForks,hasIssues,hasPulls,hasWiki,
                            hasLicense)).toString().split("\\{")[0];
            repoDtoListPaginated.setXmlLink(xmlDownloadLink);
        }

        return repoDtoListPaginated;
    }

    @Override
    public GitRepoDtoList advancedSearch(String name, Boolean nameEquals, String language, String license, String label,
                                        Long commitsMin, Long commitsMax, Long contributorsMin, Long contributorsMax,
                                        Long issuesMin, Long issuesMax, Long pullsMin, Long pullsMax, Long branchesMin,
                                        Long branchesMax, Long releasesMin, Long releasesMax, Long starsMin, Long starsMax,
                                        Long watchersMin, Long watchersMax, Long forksMin, Long forksMax, Date createdMin,
                                        Date createdMax, Date committedMin, Date committedMax, Boolean excludeForks,
                                        Boolean onlyForks, Boolean hasIssues, Boolean hasPulls, Boolean hasWiki,
                                        Boolean hasLicense){
        LongInterval commits      = new LongInterval(commitsMin,commitsMax);
        LongInterval contributors = new LongInterval(contributorsMin,contributorsMax);
        LongInterval issues       = new LongInterval(issuesMin,issuesMax);
        LongInterval pulls        = new LongInterval(pullsMin,pullsMax);
        LongInterval branches     = new LongInterval(branchesMin,branchesMax);
        LongInterval releases     = new LongInterval(releasesMin,releasesMax);
        LongInterval stars        = new LongInterval(starsMin,starsMax);
        LongInterval watchers     = new LongInterval(watchersMin,watchersMax);
        LongInterval forks        = new LongInterval(forksMin,forksMax);
        DateInterval created      = new DateInterval(createdMin,createdMax);
        DateInterval committed    = new DateInterval(committedMin,committedMax);
        List<GitRepo> repos =  gitRepoRepositoryCustom.advancedSearch(name,nameEquals,language,license,label,commits,
                                                                         contributors,issues,pulls,branches,releases,
                                                                         stars,watchers,forks,created,committed,
                                                                         excludeForks,onlyForks,hasIssues,hasPulls,
                                                                         hasWiki,hasLicense);
        List<GitRepoDto> repoDtos = gitRepoConverter.repoListToRepoDtoList(repos);
        for (GitRepoDto repoDto : repoDtos){
            repoDto.setLabels(new ArrayList<>(findRepoLabels(repoDto.getId())));
            repoDto.setLanguages(new ArrayList<>(findRepoLanguages(repoDto.getId())));
        }
        return GitRepoDtoList.builder().items(repoDtos).build();
    }

    @Override
    public GitRepo createOrUpdateRepo(GitRepo repo){
        Optional<GitRepo> opt = gitRepoRepository.findGitRepoByName(repo.getName());
        if (opt.isPresent()){
            GitRepo existing = opt.get();
            existing.setIsFork(repo.getIsFork());
            existing.setCommits(repo.getCommits());
            existing.setBranches(repo.getBranches());
            existing.setDefaultBranch(repo.getDefaultBranch());
            existing.setReleases(repo.getReleases());
            existing.setContributors(repo.getContributors());
            existing.setLicense(repo.getLicense());
            existing.setWatchers(repo.getWatchers());
            existing.setStargazers(repo.getStargazers());
            existing.setForks(repo.getForks());
            existing.setSize(repo.getSize());
            existing.setCreatedAt(repo.getCreatedAt());
            existing.setPushedAt(repo.getPushedAt());
            existing.setUpdatedAt(repo.getUpdatedAt());
            existing.setHomepage(repo.getHomepage());
            existing.setMainLanguage(repo.getMainLanguage());
            existing.setOpenIssues(repo.getOpenIssues());
            existing.setTotalIssues(repo.getTotalIssues());
            existing.setOpenPullRequests(repo.getOpenPullRequests());
            existing.setTotalPullRequests(repo.getTotalPullRequests());
            existing.setLastCommit(repo.getLastCommit());
            existing.setLastCommitSHA(repo.getLastCommitSHA());
            existing.setHasWiki(repo.getHasWiki());
            existing.setIsArchived(repo.getIsArchived());
            return gitRepoRepository.save(existing);
        } else {
            return gitRepoRepository.save(repo);
        }
    }

    @Override
    public StringList getAllLabels(){
        return StringList.builder().items(gitRepoLabelRepository.findAllLabels()).build();
    }

    @Override
    public StringList getAllLanguages(){
        return StringList.builder().items(gitRepoLanguageRepository.findAllLanguages()).build();
    }

    @Override
    public StringLongDtoList getAllLanguageStatistics(){
        List<Object[]> languages = gitRepoLanguageRepository.getLanguageStatistics();
        StringLongDtoList stats = StringLongDtoList.builder().build();
        languages.forEach(language -> stats.getItems().add(new StringLongDto((String) language[0],(Long) language[1])));
        return stats;
    }

    @Override
    public StringLongDtoList getMainLanguageStatistics(){
        List<Object[]> languages = gitRepoRepository.getLanguageStatistics();
        StringLongDtoList stats = StringLongDtoList.builder().build();
        languages.forEach(language -> stats.getItems().add(new StringLongDto((String) language[0],(Long) language[1])));
        return stats;
    }

    @Override
    @Transactional
    public void createUpdateLabels(GitRepo repo, List<GitRepoLabel> labels){
        gitRepoLabelRepository.deleteAllByRepo(repo);
        gitRepoLabelRepository.saveAll(labels);
    }

    @Override
    @Transactional
    public void createUpdateLanguages(GitRepo repo, List<GitRepoLanguage> languages){
        gitRepoLanguageRepository.deleteAllByRepo(repo);
        gitRepoLanguageRepository.saveAll(languages);
    }
}
