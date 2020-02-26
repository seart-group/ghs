package com.dabico.githubseapp.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "programming_language")
@Entity
public class ProgrammingLanguage {
    @Id
    @GeneratedValue
    @Column(name = "language_id")
    Long id;

    @Column(name = "language_name", unique = true)
    String name;
}
