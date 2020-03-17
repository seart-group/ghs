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
@Table(name = "repo_label")
@Entity
public class GitRepoLabel {
    @Id
    @GeneratedValue
    @Column(name = "repo_label_id")
    Long id;

    @ManyToOne
    GitRepo repo;

    @Column(name = "repo_label_name")
    String label;

    @Column(name = "crawled")
    Date crawled;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { crawled = new Date(); }
}
