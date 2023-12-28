package ch.usi.si.seart.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepoDto {

    Long id;

    String name;

    Boolean isFork;

    Long commits;
    Long branches;
    Long releases;
    Long forks;

    String mainLanguage;
    String defaultBranch;

    String license;
    String homepage;

    Long watchers;
    Long stargazers;
    Long contributors;

    Long size;

    Date createdAt;
    Date pushedAt;
    Date updatedAt;

    Long totalIssues;
    Long openIssues;

    Long totalPullRequests;
    Long openPullRequests;

    Long blankLines;
    Long codeLines;
    Long commentLines;

    @JacksonXmlElementWrapper(localName = "metrics")
    @JacksonXmlProperty(localName = "metric")
    @Builder.Default
    List<Map<String, Object>> metrics = new ArrayList<>();

    Date lastCommit;
    String lastCommitSHA;

    Boolean hasWiki;
    Boolean isArchived;
    Boolean isDisabled;

    @JacksonXmlProperty(localName = "languages", isAttribute = true)
    @Builder.Default
    Map<String, Long> languages = new LinkedHashMap<>();

    @JacksonXmlElementWrapper(localName = "labels")
    @JacksonXmlProperty(localName = "label")
    @Builder.Default
    Set<String> labels = new TreeSet<>();

    @JacksonXmlElementWrapper(localName = "topics")
    @JacksonXmlProperty(localName = "topic")
    @Builder.Default
    Set<String> topics = new TreeSet<>();
}
