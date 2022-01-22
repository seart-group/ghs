package usi.si.seart.gseapp.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVWriter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.dto.GitRepoDto;
import usi.si.seart.gseapp.dto.GitRepoDtoList;
import usi.si.seart.gseapp.dto.GitRepoLabelDto;
import usi.si.seart.gseapp.dto.GitRepoLanguageDto;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;
import usi.si.seart.gseapp.model.GitRepoLanguage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitRepoConverter {
    static String[] gitRepoFields;
    static ObjectMapper objectMapper;
    static XmlMapper xmlMapper;
    static {
        List<String> gitRepoFieldsList = Arrays.stream(GitRepo.class.getDeclaredFields())
                .map(field -> {
                    String name = field.getName();
                    String[] words  = StringUtils.splitByCharacterTypeCamelCase(name);
                    name = StringUtils.join(words, " ");
                    return StringUtils.capitalize(name);
                }).collect(Collectors.toList());
        gitRepoFieldsList.remove(gitRepoFieldsList.size() - 1);
        gitRepoFieldsList.remove(0);
        gitRepoFieldsList.add("Languages");
        gitRepoFieldsList.add("Labels");
        gitRepoFields = gitRepoFieldsList.toArray(new String[0]);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(df);
        xmlMapper = new XmlMapper();
        xmlMapper.setDateFormat(df);
    }

    public File repoListToJSON(File file, GitRepoDtoList repos) throws IOException {
        return repoListToFile(objectMapper, file, repos);
    }

    public File repoListToXML(File file, GitRepoDtoList repos) throws IOException {
        return repoListToFile(xmlMapper, file, repos);
    }

    public File repoListToCSV(File file, GitRepoDtoList repos) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsolutePath()));
        List<String[]> rows = repos.getItems().stream().map(this::repoDtoToCSVRow).collect(Collectors.toList());
        rows.add(0, gitRepoFields);
        writer.writeAll(rows);
        writer.close();
        return file;
    }

    private File repoListToFile(ObjectMapper objectMapper, File file, GitRepoDtoList repos) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, repos);
        return file;
    }

    public List<GitRepoDto> repoListToRepoDtoList(List<GitRepo> repos){
        return repos.stream().map(this::repoToRepoDto).collect(Collectors.toList());
    }

    public List<GitRepoLabelDto> labelListToLabelDtoList(List<GitRepoLabel> labels){
        return labels.stream().map(this::labelToLabelDto).collect(Collectors.toList());
    }

    public List<GitRepoLanguageDto> languageListToLanguageDtoList(List<GitRepoLanguage> languages){
        return languages.stream().map(this::languageToLanguageDto).collect(Collectors.toList());
    }

    public GitRepoDto repoToRepoDto(GitRepo repo){
        return GitRepoDto.builder()
                .id(repo.getId())
                .name(repo.getName())
                .isFork(repo.getIsFork())
                .commits(repo.getCommits())
                .branches(repo.getBranches())
                .defaultBranch(repo.getDefaultBranch())
                .releases(repo.getReleases())
                .contributors(repo.getContributors())
                .license(repo.getLicense())
                .watchers(repo.getWatchers())
                .stargazers(repo.getStargazers())
                .forks(repo.getForks())
                .size(repo.getSize())
                .createdAt(repo.getCreatedAt())
                .pushedAt(repo.getPushedAt())
                .updatedAt(repo.getUpdatedAt())
                .homepage(repo.getHomepage())
                .mainLanguage(repo.getMainLanguage())
                .totalIssues(repo.getTotalIssues())
                .openIssues(repo.getOpenIssues())
                .totalPullRequests(repo.getTotalPullRequests())
                .openPullRequests(repo.getOpenPullRequests())
                .lastCommit(repo.getLastCommit())
                .lastCommitSHA(repo.getLastCommitSHA())
                .hasWiki(repo.getHasWiki())
                .isArchived(repo.getIsArchived())
                .build();
    }

    private String[] repoDtoToCSVRow(GitRepoDto repoDto){
        List<GitRepoLanguageDto> languages = repoDto.getLanguages();
        List<GitRepoLabelDto> labels = repoDto.getLabels();

        String[] attributes = new String[27];
        attributes[0]  = repoDto.getName();
        attributes[1]  = Objects.toString(repoDto.getIsFork(), "");
        attributes[2]  = Objects.toString(repoDto.getCommits(), "");
        attributes[3]  = Objects.toString(repoDto.getBranches(), "");
        attributes[4]  = Objects.toString(repoDto.getDefaultBranch(), "");
        attributes[5]  = Objects.toString(repoDto.getReleases(), "");
        attributes[6]  = Objects.toString(repoDto.getContributors(), "");
        attributes[7]  = Objects.toString(repoDto.getLicense(), "");
        attributes[8]  = Objects.toString(repoDto.getWatchers(), "");
        attributes[9]  = Objects.toString(repoDto.getStargazers(), "");
        attributes[10] = Objects.toString(repoDto.getForks(), "");
        attributes[11] = Objects.toString(repoDto.getSize(), "");
        attributes[12] = Objects.toString(repoDto.getCreatedAt(), "");
        attributes[13] = Objects.toString(repoDto.getPushedAt(), "");
        attributes[14] = Objects.toString(repoDto.getUpdatedAt(), "");
        attributes[15] = Objects.toString(repoDto.getHomepage(), "");
        attributes[16] = Objects.toString(repoDto.getMainLanguage(), "");
        attributes[17] = Objects.toString(repoDto.getTotalIssues(), "");
        attributes[18] = Objects.toString(repoDto.getOpenIssues(), "");
        attributes[19] = Objects.toString(repoDto.getTotalPullRequests(), "");
        attributes[20] = Objects.toString(repoDto.getOpenPullRequests(), "");
        attributes[21] = Objects.toString(repoDto.getLastCommit(), "");
        attributes[22] = Objects.toString(repoDto.getLastCommitSHA(), "");
        attributes[23] = Objects.toString(repoDto.getHasWiki(), "");
        attributes[24] = Objects.toString(repoDto.getIsArchived(), "");
        attributes[25] = languages.stream().map(GitRepoLanguageDto::getLanguage).collect(Collectors.joining(","));
        attributes[26] = labels.stream().map(GitRepoLabelDto::getLabel).collect(Collectors.joining(","));
        return attributes;
    }

    public GitRepoLabelDto labelToLabelDto(GitRepoLabel label){
        return GitRepoLabelDto.builder()
                              .id(label.getId())
                              .label(label.getLabel())
                              .build();
    }

    public GitRepoLanguageDto languageToLanguageDto(GitRepoLanguage language){
        return GitRepoLanguageDto.builder()
                                 .id(language.getId())
                                 .language(language.getLanguage())
                                 .sizeOfCode(language.getSizeOfCode())
                                 .build();
    }
}
