package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topics")
@Entity
public class Topic {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "label")
    String label;

    @OneToMany(mappedBy = "topic")
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoTopic> topics = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return id.equals(((Topic) obj).id)
                && label.equals(((Topic) obj).label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }
}
