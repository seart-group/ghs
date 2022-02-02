package usi.si.seart.gseapp.dto;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class GitRepoDto {
    Long id;
    String name;
    Boolean isFork;
    Long commits;
    Long branches;
    String defaultBranch;
    Long releases;
    Long contributors;
    String license;
    Long watchers;
    Long stargazers;
    Long forks;
    Long size;
    Date createdAt;
    Date pushedAt;
    Date updatedAt;
    String homepage;
    String mainLanguage;
    Long totalIssues;
    Long openIssues;
    Long totalPullRequests;
    Long openPullRequests;
    Date lastCommit;
    String lastCommitSHA;
    Boolean hasWiki;
    Boolean isArchived;
    @Builder.Default
    Map<String, Long> languages = new LinkedHashMap<>();
    @Builder.Default
    List<String> labels = new ArrayList<>();

    public static CsvSchema getCsvSchema(){
        Set<String> exclusions = Set.of("id", "labels", "languages");

        List<String> fields = Arrays.stream(GitRepoDto.class.getDeclaredFields())
                .map(Field::getName)
                .filter(Predicate.not(exclusions::contains))
                .collect(Collectors.toList());

        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        for (String field : fields){
            schemaBuilder.addColumn(field);
        }

        return schemaBuilder.build().withHeader();
    }
}
