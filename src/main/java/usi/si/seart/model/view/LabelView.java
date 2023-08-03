package usi.si.seart.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Subselect("SELECT name FROM label_ranked")
public class LabelView {

    @Id
    private String name;
}
