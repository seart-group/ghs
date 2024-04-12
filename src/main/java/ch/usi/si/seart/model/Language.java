package ch.usi.si.seart.model;

import ch.usi.si.seart.model.join.GitRepoLanguage;
import ch.usi.si.seart.model.join.GitRepoMetric;
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
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "language")
@Entity
public class Language {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @NotBlank
    @Column(name = "name", unique = true)
    String name;

    @Builder.Default
    @OneToMany(mappedBy = "language")
    Set<GitRepoLanguage> repos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "language")
    Set<GitRepoMetric> metrics = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(
            mappedBy = "language",
            cascade = {
                CascadeType.PERSIST,
                CascadeType.MERGE,
                CascadeType.REFRESH,
                CascadeType.DETACH
            }
    )
    Statistics statistics;

    @PrimaryKeyJoinColumn
    @OneToOne(
            mappedBy = "language",
            cascade = {
                CascadeType.PERSIST,
                CascadeType.MERGE,
                CascadeType.REFRESH,
                CascadeType.DETACH
            }
    )
    Progress progress;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Immutable
    @Table(name = "language_statistics")
    public static class Statistics {

        @Id
        @Column(name = "language_id")
        Long id;

        @OneToOne(optional = false)
        @MapsId("id")
        @JoinColumn(name = "language_id")
        Language language;

        @Builder.Default
        @Column(name = "mined")
        Long mined = 0L;

        @Builder.Default
        @Column(name = "analyzed")
        Long analyzed = 0L;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            Statistics statistics = (Statistics) o;
            return getId() != null && Objects.equals(getId(), statistics.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getId());
        }
    }

    @Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
    @StaticMetamodel(Statistics.class)
    public abstract class Statistics_ {

        public static volatile SingularAttribute<Statistics, Long> id;
        public static volatile SingularAttribute<Statistics, Long> mined;
        public static volatile SingularAttribute<Statistics, Long> analyzed;
        public static volatile SingularAttribute<Statistics, Language> language;

        public static final String ID = "id";
        public static final String MINED = "mined";
        public static final String ANALYZED = "analyzed";
        public static final String LANGUAGE = "language";
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "language_progress")
    public static class Progress {

        @Id
        @Column(name = "language_id")
        Long id;

        @OneToOne(optional = false)
        @MapsId("id")
        @JoinColumn(name = "language_id")
        Language language;

        @NotNull
        @PastOrPresent
        @Column(name = "checkpoint")
        Date checkpoint;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            Progress progress = (Progress) o;
            return getId() != null && Objects.equals(getId(), progress.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Language language = (Language) o;
        return getId() != null && Objects.equals(getId(), language.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
