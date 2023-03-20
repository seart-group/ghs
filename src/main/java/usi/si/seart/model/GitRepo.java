package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "repo")
@Entity
public class GitRepo {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "name")
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

    @Builder.Default
    @OneToMany(mappedBy="repo", cascade=CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoLabel> labels = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy="repo", cascade= CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoLanguage> languages = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepo gitRepo = (GitRepo) o;
        return id != null && Objects.equals(id, gitRepo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() {
        crawled = new Date();
    }
}
