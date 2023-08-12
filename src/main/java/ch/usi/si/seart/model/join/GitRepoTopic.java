package ch.usi.si.seart.model.join;

import ch.usi.si.seart.model.GitRepo;
import ch.usi.si.seart.model.Topic;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "git_repo_topic")
@Entity
public class GitRepoTopic {

    @EmbeddedId
    Key key;

    @ManyToOne(optional = false)
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(optional = false)
    @MapsId("topicId")
    @JoinColumn(name = "topic_id")
    Topic topic;

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
        @Column(name = "topic_id")
        Long topicId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Key other = (Key) obj;
            return repoId.equals(other.repoId) && topicId.equals(other.topicId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(repoId, topicId);
        }
    }
}
