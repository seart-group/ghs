package ch.usi.si.seart.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@Table(name = "git_repo_metrics_by_id")
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
