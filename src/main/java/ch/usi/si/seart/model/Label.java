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

import javax.annotation.Generated;
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
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
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
@Table(name = "label")
@Entity
public class Label {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @NotBlank
    @Column(name = "name", unique = true)
    String name;

    @Builder.Default
    @ManyToMany(mappedBy = "labels")
    Set<GitRepo> repos = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(
            mappedBy = "label",
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
    @Table(name = "label_statistics")
    public static class Statistics {

        @Id
        @Column(name = "label_id")
        Long id;

        @OneToOne(optional = false)
        @MapsId("id")
        @JoinColumn(name = "label_id")
        Label label;

        @Builder.Default
        @Column(name = "count")
        Long count = 0L;
    }

    @Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
    @StaticMetamodel(Statistics.class)
    public abstract class Statistics_ {

        public static volatile SingularAttribute<Statistics, Long> id;
        public static volatile SingularAttribute<Statistics, Long> count;
        public static volatile SingularAttribute<Statistics, Label> label;

        public static final String ID = "id";
        public static final String COUNT = "count";
        public static final String LABEL = "label";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Label label = (Label) o;
        return getId() != null && Objects.equals(getId(), label.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
