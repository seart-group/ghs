package ch.usi.si.seart.model.join;

import ch.usi.si.seart.model.GitRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "git_repo_metric_aggregate")
public class GitRepoMetricAggregate {

    @Id
    @Column(name = "repo_id")
    Long id;

    @OneToOne(optional = false)
    @MapsId("id")
    @JoinColumn(name = "repo_id")
    GitRepo repo;

    @Column(name = "lines")
    Long lines;

    @Column(name = "lines_blank")
    Long blankLines;

    @Column(name = "lines_non_blank")
    Long nonBlankLines;

    @Column(name = "lines_code")
    Long codeLines;

    @Column(name = "lines_comment")
    Long commentLines;
}
