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
 * Composite key for the repo tag.
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GitRepoTopicKey implements Serializable {
    @Column(name = "repo_id")
    Long repoId;

    @Column(name = "topic_id")
    Long topicId;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoTopicKey key = (GitRepoTopicKey) obj;

        return repoId.equals(key.repoId) && topicId.equals(key.topicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoId, topicId);
    }
}
