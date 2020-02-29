package com.dabico.githubseapp.model;

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
@Table(name = "repo_languages")
@Entity
public class RepositoryLanguage {
    @Id
    @Column(name = "repo_language_id")
    Long id;

    @Column(name = "repo_id")
    Long repositoryId;

    @Column(name = "repo_language")
    String language;

    @Column(name = "size_of_code")
    Long sizeOfCode;
}
