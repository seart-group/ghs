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
@Table(name = "access_token")
@Entity
public class AccessToken {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @Column(name = "token")
    String token;

    @Column(name = "added")
    Date added;

    @PreUpdate
    @PrePersist
    private void onPersistAndUpdate() { added = new Date(); }
}
