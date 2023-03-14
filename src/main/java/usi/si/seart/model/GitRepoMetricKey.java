package usi.si.seart.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for the metric.
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GitRepoMetricKey implements Serializable {
    @Column(name = "repo_id")
    Long repoId;

    @Column(name = "metric_language_id")
    Long metricLanguageId;


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoMetricKey key = (GitRepoMetricKey) obj;

        return repoId.equals(key.repoId) && metricLanguageId.equals(key.metricLanguageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoId, metricLanguageId);
    }
}
