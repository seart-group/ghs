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
import javax.validation.constraints.NotNull;
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

    @NotNull
    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "language")
    Set<GitRepoLanguage> repos = new HashSet<>();

    @OneToMany(mappedBy = "language")
    Set<GitRepoMetric> metrics = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "language")
    Statistics statistics;

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
