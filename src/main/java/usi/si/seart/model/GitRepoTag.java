package usi.si.seart.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Objects;



@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "repo_tags")
@Entity
public class GitRepoTag {

    @EmbeddedId
    GitRepoTagKey id;

    @ManyToOne
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    Tag tag;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoTag metric = (GitRepoTag) obj;

        return id.equals(metric.id)
                && repo.equals(metric.repo)
                && tag.equals(metric.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo, tag);
    }
}