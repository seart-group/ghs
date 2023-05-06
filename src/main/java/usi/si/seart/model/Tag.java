package usi.si.seart.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
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
public class Tag {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "label")
    String label;

    @OneToMany(mappedBy = "language")
    @Fetch(value = FetchMode.JOIN)
    Set<GitRepoMetric> metrics = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        return id.equals(((Tag) obj).id)
                && label.equals(((Tag) obj).label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }
}
