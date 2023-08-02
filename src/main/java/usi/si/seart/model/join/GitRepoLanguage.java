package usi.si.seart.model.join;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "git_repo_language")
@Entity
public class GitRepoLanguage {

    @EmbeddedId
    Key key;

    @ManyToOne(optional = false)
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(optional = false)
    @MapsId("languageId")
    @JoinColumn(name = "language_id")
    Language language;

    @NotNull
    @Column(name = "size_of_code")
    Long sizeOfCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepoLanguage language = (GitRepoLanguage) o;
        return getKey() != null && Objects.equals(getKey(), language.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class Key implements Serializable {

        @NotNull
        @Column(name = "repo_id")
        Long repoId;

        @NotNull
        @Column(name = "language_id")
        Long languageId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Key other = (Key) obj;
            return repoId.equals(other.repoId) && languageId.equals(other.languageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(repoId, languageId);
        }
    }
}
