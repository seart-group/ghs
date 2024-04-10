package ch.usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "topic")
@Entity
public class Topic {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @NotBlank
    @Column(name = "name", unique = true)
    String name;

    @Builder.Default
    @ManyToMany(mappedBy = "topics")
    Set<GitRepo> repos = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(
            mappedBy = "topic",
            cascade = {
                CascadeType.PERSIST,
                CascadeType.MERGE,
                CascadeType.REFRESH,
                CascadeType.DETACH
            }
    )
    Statistics statistics;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Immutable
    @Table(name = "topic_statistics")
    public static class Statistics {

        @Id
        @Column(name = "topic_id")
        Long id;

        @OneToOne(optional = false)
        @MapsId("id")
        @JoinColumn(name = "topic_id")
        Topic topic;

        @Builder.Default
        @Column(name = "count")
        Long count = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Topic topic = (Topic) o;
        return getId() != null && Objects.equals(getId(), topic.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
