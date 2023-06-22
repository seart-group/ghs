package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
@Table(name = "repo_metrics")
@Entity
public class GitRepoMetric {

    @EmbeddedId
    Key key;

    @ManyToOne(optional = false)
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(optional = false)
    @MapsId("metricLanguageId")
    @JoinColumn(name = "metric_language_id")
    MetricLanguage language;

    @Column(name = "lines")
    Long lines;

    @Column(name = "lines_non_blank")
    Long nonBlankLines;

    @Column(name = "lines_blank")
    Long blankLines;

    @Column(name = "lines_code")
    Long codeLines;

    @Column(name = "lines_comment")
    Long commentLines;

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
    @Embeddable
    public static class Key implements Serializable {

        @NotNull
        @Column(name = "repo_id")
        Long repoId;

        @NotNull
        @Column(name = "metric_language_id")
        Long metricLanguageId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GitRepoMetric.Key other = (GitRepoMetric.Key) obj;
            return repoId.equals(other.repoId) && metricLanguageId.equals(other.metricLanguageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(repoId, metricLanguageId);
        }
    }
}
