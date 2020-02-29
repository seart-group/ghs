package com.dabico.gseapp.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
}
