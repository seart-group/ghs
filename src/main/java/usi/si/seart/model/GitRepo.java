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
import org.hibernate.annotations.Formula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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

    @Column(name = "cloned")
    Date cloned;

    @Builder.Default
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @JoinTable(
        name = "repo_label",
        joinColumns = @JoinColumn(name = "repo_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    @Fetch(value = FetchMode.JOIN)
    Set<Label> labels = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "repo", cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoLanguage> languages = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "repo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoMetric> metrics = new HashSet<>();

    @Builder.Default
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @JoinTable(
        name = "repo_topic",
        joinColumns = @JoinColumn(name = "repo_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Fetch(value = FetchMode.JOIN)
    Set<Topic> topics = new HashSet<>();

    @Formula("(select m.lines from repo_metrics_by_repo m where m.repo_id = id)")
    Long lines;

    @Formula("(select m.lines_code from repo_metrics_by_repo m where m.repo_id = id)")
    Long codeLines;

    @Formula("(select m.lines_comment from repo_metrics_by_repo m where m.repo_id = id)")
    Long commentLines;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepo gitRepo = (GitRepo) o;
        return getId() != null && Objects.equals(getId(), gitRepo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * To be called when the repository has been crawled through GitHub's API.
     */
    public void setCrawled() {
        crawled = new Date();
    }

    /**
     * To be called when the repository's code metrics have been mined.
     */
    public void setCloned() {
        cloned = new Date();
    }
}
