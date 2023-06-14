package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Formula;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
    GitRepoMetricKey id;

    @ManyToOne
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("metricLanguageId")
    @JoinColumn(name = "metric_language_id")
    MetricLanguage language;

    @Column(name = "lines_blank")
    Long blankLines;

    @Column(name = "lines_code")
    Long codeLines;

    @Column(name = "lines_comment")
    Long commentLines;

    @Formula("(lines_code + lines_comment)")
    Long totalLines;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoMetric metric = (GitRepoMetric) obj;

        return id.equals(metric.id)
                && repo.equals(metric.repo)
                && language.equals(metric.language)
                && blankLines.equals(metric.blankLines)
                && codeLines.equals(metric.codeLines)
                && commentLines.equals(metric.commentLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo, language, blankLines, codeLines, commentLines);
    }
}
