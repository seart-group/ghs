package usi.si.seart.gseapp.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "repo_label")
@Entity
public class GitRepoLabel {
    @Id
    @GeneratedValue
    @Column(name = "repo_label_id")
    Long id;

    @EqualsAndHashCode.Include
    @ManyToOne
    GitRepo repo;

    @EqualsAndHashCode.Include
    @Column(name = "repo_label_name")
    String label;

    @Column(name = "crawled")
    Date crawled;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { crawled = new Date(); }
}
