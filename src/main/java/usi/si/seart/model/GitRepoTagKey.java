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
public class GitRepoTagKey implements Serializable {
    @Column(name = "repo_id")
    Long repoId;

    @Column(name = "tag_id")
    Long tagId;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoTagKey key = (GitRepoTagKey) obj;

        return repoId.equals(key.repoId) && tagId.equals(key.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoId, tagId);
    }
}
