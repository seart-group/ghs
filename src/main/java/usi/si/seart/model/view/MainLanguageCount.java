package usi.si.seart.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Subselect("SELECT name, count FROM count_repo_by_main_language")
public class MainLanguageCount {

    @Id
    private String name;

    @NotNull
    @PositiveOrZero
    private Long count;
}
