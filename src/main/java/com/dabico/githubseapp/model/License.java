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
@Table(name = "license")
@Entity
public class License {
    @Id
    @GeneratedValue
    @Column(name = "license_id")
    Long id;

    @Column(name = "license_name", unique = true)
    String name;

    @Column(name = "license_keyword", unique = true)
    String keyword;
}
