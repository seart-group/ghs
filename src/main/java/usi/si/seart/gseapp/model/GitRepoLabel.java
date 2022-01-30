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
@Table(name = "repo_label")
@Entity
public class GitRepoLabel {
    @Id
    @GeneratedValue
    @Column(name = "repo_label_id")
    Long id;

    @ManyToOne
    GitRepo repo;

    @Column(name = "repo_label_name")
    String label;

    @Column(name = "crawled")
    Date crawled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepoLabel that = (GitRepoLabel) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo.getId(), label);
    }

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() {
        crawled = new Date();
    }
}
