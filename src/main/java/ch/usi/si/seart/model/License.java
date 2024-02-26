package ch.usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
}
