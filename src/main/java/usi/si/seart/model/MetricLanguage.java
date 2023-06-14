package usi.si.seart.model;

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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "metric_language")
@Entity
public class MetricLanguage {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "language")
    String language;

    @OneToMany(mappedBy = "language")
    Set<GitRepoMetric> metrics = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        return id.equals(((MetricLanguage) obj).id)
                && language.equals(((MetricLanguage) obj).language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, language);
    }
}
