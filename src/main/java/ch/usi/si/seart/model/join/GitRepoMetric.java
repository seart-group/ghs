package ch.usi.si.seart.model.join;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Language;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.util.Objects;

/**
 * A metric for one language of a git repository.
 * It is a junction table for the ManyToMany relationship between GitRepo and MetricLanguage.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "git_repo_metric")
@Entity
public class GitRepoMetric {

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

    @Generated(value = GenerationTime.ALWAYS)
    @Column(
            name = "lines",
            insertable = false,
            updatable = false
    )
    Long lines;

    @Generated(value = GenerationTime.ALWAYS)
    @Column(
            name = "lines_non_blank",
            insertable = false,
            updatable = false
    )
    Long nonBlankLines;

    @NotNull
    @PositiveOrZero
    @Column(name = "lines_blank")
    @Builder.Default
    Long blankLines = 0L;

    @NotNull
    @PositiveOrZero
    @Column(name = "lines_code")
    @Builder.Default
    Long codeLines = 0L;

    @NotNull
    @PositiveOrZero
    @Column(name = "lines_comment")
    @Builder.Default
    Long commentLines = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GitRepoMetric metric = (GitRepoMetric) o;
        return getKey() != null && Objects.equals(getKey(), metric.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
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
            GitRepoMetric.Key other = (GitRepoMetric.Key) obj;
            return repoId.equals(other.repoId) && languageId.equals(other.languageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(repoId, languageId);
        }
    }
}
