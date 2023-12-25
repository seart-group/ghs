package ch.usi.si.seart.model;

import ch.usi.si.seart.model.join.GitRepoLanguage;
import ch.usi.si.seart.model.join.GitRepoMetric;
import ch.usi.si.seart.model.join.GitRepoMetricAggregate;
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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
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
@Table(name = "git_repo")
@Entity
public class GitRepo {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id")
    Language mainLanguage;

    @NotBlank
    @Column(name = "name", unique = true)
    String name;

    @Column(name = "is_fork_project")
    Boolean isFork;

    @PositiveOrZero
    @Column(name = "commits")
    Long commits;

    @PositiveOrZero
    @Column(name = "branches")
    Long branches;

    @NotBlank
    @Column(name = "default_branch")
    String defaultBranch;

    @PositiveOrZero
    @Column(name = "releases")
    Long releases;

    @PositiveOrZero
    @Column(name = "contributors")
    Long contributors;

    @Column(name = "license")
    String license;

    @PositiveOrZero
    @Column(name = "watchers")
    Long watchers;

    @PositiveOrZero
    @Column(name = "stargazers")
    Long stargazers;

    @PositiveOrZero
    @Column(name = "forks")
    Long forks;

    @PositiveOrZero
    @Column(name = "size")
    Long size;

    @PastOrPresent
    @Column(name = "created_at")
    Date createdAt;

    @PastOrPresent
    @Column(name = "pushed_at")
    Date pushedAt;

    @Column(name = "updated_at")
    Date updatedAt;

    @Column(name = "homepage")
    String homepage;

    @PositiveOrZero
    @Column(name = "total_issues")
    Long totalIssues;

    @PositiveOrZero
    @Column(name = "open_issues")
    Long openIssues;

    @PositiveOrZero
    @Column(name = "total_pull_requests")
    Long totalPullRequests;

    @PositiveOrZero
    @Column(name = "open_pull_requests")
    Long openPullRequests;

    @PastOrPresent
    @Column(name = "last_commit")
    Date lastCommit;

    @Column(name = "last_commit_sha")
    String lastCommitSHA;

    @Column(name = "has_wiki")
    Boolean hasWiki;

    @Column(name = "archived")
    Boolean isArchived;

    @PastOrPresent
    @Column(name = "last_pinged")
    Date lastPinged;

    @PastOrPresent
    @Column(name = "last_analyzed")
    Date lastAnalyzed;

    @Builder.Default
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @JoinTable(
        name = "git_repo_label",
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
    @OneToMany(mappedBy = "repo", cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoMetric> metrics = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "repo")
    @Fetch(value = FetchMode.JOIN)
    GitRepoMetricAggregate totalMetrics;

    @Builder.Default
    @ManyToMany(cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.DETACH
    })
    @JoinTable(
        name = "git_repo_topic",
        joinColumns = @JoinColumn(name = "repo_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @Fetch(value = FetchMode.JOIN)
    Set<Topic> topics = new HashSet<>();

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
     * To be called whenever the repository is encountered either in the API or during cleaning and analysis.
     */
    @PrePersist
    @PreUpdate
    public void setLastPinged() {
        lastPinged = new Date();
    }

    /**
     * To be called when the repository's code metrics have been mined.
     */
    public void setLastAnalyzed() {
        lastAnalyzed = new Date();
    }
}
