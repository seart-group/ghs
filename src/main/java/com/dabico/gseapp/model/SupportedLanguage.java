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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "supported_language")
@Entity
public class SupportedLanguage {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @EqualsAndHashCode.Include
    @Column(name = "name")
    String name;

    @Column(name = "added")
    Date added;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { added = new Date(); }
}
