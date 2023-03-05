package usi.si.seart.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
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
    @Builder.Default
    @EmbeddedId
    GitRepoMetricKey id = new GitRepoMetricKey();

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


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return id.equals(((GitRepoMetric) obj).id)
                && repo.equals(((GitRepoMetric) obj).repo)
                && language.equals(((GitRepoMetric) obj).language)
                && blankLines.equals(((GitRepoMetric) obj).blankLines)
                && codeLines.equals(((GitRepoMetric) obj).codeLines)
                && commentLines.equals(((GitRepoMetric) obj).commentLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo, language, blankLines, codeLines, commentLines);
    }

}


