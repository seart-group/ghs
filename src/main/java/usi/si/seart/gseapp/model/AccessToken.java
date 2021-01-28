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
@Table(name = "access_token")
@Entity
public class AccessToken {

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @EqualsAndHashCode.Include
    @Column(name = "value")
    String value;

    @Column(name = "added")
    Date added;
}
