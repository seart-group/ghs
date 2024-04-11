package ch.usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

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
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "license")
@Entity
public class License {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @NotBlank
    @Column(name = "name", unique = true)
    String name;

    @Builder.Default
    @OneToMany(mappedBy = "license")
    Set<GitRepo> repos = new HashSet<>();

    @PrimaryKeyJoinColumn
    @OneToOne(
            mappedBy = "license",
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
    @Table(name = "license_statistics")
    public static class Statistics {

        @Id
        @Column(name = "license_id")
        Long id;

        @OneToOne(optional = false)
        @MapsId("id")
        @JoinColumn(name = "license_id")
        License license;

        @Builder.Default
        @Column(name = "count")
        Long count = 0L;
    }
}
