package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "hibernate_sequence"
    )
    @SequenceGenerator(
        name = "hibernate_sequence",
        allocationSize = 1
    )
    @Column(name = "id")
    Long id;

    @NotNull
    @Column(name = "name")
    String name;

    @Builder.Default
    @OneToMany(mappedBy = "language")
    Set<GitRepoLanguage> repos = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "language")
    Set<GitRepoMetric> metrics = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "language")
    Statistics statistics;

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "language")
    Progress progress;

    @Getter
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

        @Column(name = "mined")
        Long mined;

        @Column(name = "analyzed")
        Long analyzed;

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
