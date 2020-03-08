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
@Table(name = "repo_language")
@Entity
public class GitRepoLanguage {
    @Id
    @Column(name = "repo_language_id")
    Long id;

    @Column(name = "repo_id")
    Long repositoryId;

    @Column(name = "repo_language_name")
    String language;

    @Column(name = "size_of_code")
    Long sizeOfCode;

    @Column(name = "crawled")
    Date crawled;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { crawled = new Date(); }
}
