package usi.si.seart.gseapp.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "repo")
@Entity
public class GitRepo {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "name")
    @EqualsAndHashCode.Include
    String name;

    @Column(name = "is_fork_project")
    Boolean isFork;

    @Column(name = "commits")
    Long commits;

    @Column(name = "branches")
    Long branches;

    @Column(name = "default_branch")
    String defaultBranch;

    @Column(name = "releases")
    Long releases;

    @Column(name = "contributors")
    Long contributors;

    @Column(name = "license")
    String license;

    @Column(name = "watchers")
    Long watchers;

    @Column(name = "stargazers")
    Long stargazers;

    @Column(name = "forks")
    Long forks;

    @Column(name = "size")
    Long size;

    @Column(name = "created_at")
    Date createdAt;

    @Column(name = "pushed_at")
    Date pushedAt;

    @Column(name = "updated_at")
    Date updatedAt;

    @Column(name = "homepage")
    String homepage;

    @Column(name = "main_language")
    String mainLanguage;

    @Column(name = "total_issues")
    Long totalIssues;

    @Column(name = "open_issues")
    Long openIssues;

    @Column(name = "total_pull_requests")
    Long totalPullRequests;

    @Column(name = "open_pull_requests")
    Long openPullRequests;

    @Column(name = "last_commit")
    Date lastCommit;

    @Column(name = "last_commit_sha")
    String lastCommitSHA;

    @Column(name = "has_wiki")
    Boolean hasWiki;

    @Column(name = "archived")
    Boolean isArchived;

    @Column(name = "crawled")
    Date crawled;

//    @Builder.Default
//    @OneToMany(mappedBy="repo", cascade=CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    Set<GitRepoLabel> labels = new HashSet<>();

//    @Builder.Default
//    @OneToMany(mappedBy="repo", cascade=CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    Set<GitRepoLanguage> languages = new HashSet<>();

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { crawled = new Date(); }
}
