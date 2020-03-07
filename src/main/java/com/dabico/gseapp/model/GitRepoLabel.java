package com.dabico.gseapp.model;

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
@EqualsAndHashCode
@Table(name = "repo_labels")
@Entity
public class GitRepoLabel {
    @Id
    @Column(name = "repo_label_id")
    Long id;

    @Column(name = "repo_id")
    Long repositoryId;

    @Column(name = "repo_label")
    String label;

    @Column(name = "crawled")
    Date crawled;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { crawled = new Date(); }
}
