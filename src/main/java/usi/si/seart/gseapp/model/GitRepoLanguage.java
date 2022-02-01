package usi.si.seart.gseapp.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "repo_language")
@Entity
public class GitRepoLanguage {
    @Id
    @GeneratedValue
    @Column(name = "repo_language_id")
    Long id;

    @ManyToOne
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @Column(name = "repo_language_name")
    String language;

    @Column(name = "size_of_code")
    Long sizeOfCode;

    @Column(name = "crawled")
    Date crawled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepoLanguage that = (GitRepoLanguage) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo.getId(), language);
    }

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() {
        crawled = new Date();
    }
}
