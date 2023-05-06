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
@Table(name = "repo_topics")
@Entity
public class GitRepoTopic {

    @EmbeddedId
    GitRepoTopicKey id;

    @ManyToOne
    @MapsId("repoId")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("topicId")
    @JoinColumn(name = "topic_id")
    Topic topic;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GitRepoTopic metric = (GitRepoTopic) obj;

        return id.equals(metric.id)
                && repo.equals(metric.repo)
                && topic.equals(metric.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, repo, topic);
    }
}