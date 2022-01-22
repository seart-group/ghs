package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;

import java.util.List;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel,Long> {
    @Query("select l from GitRepoLabel l left join GitRepo r on l.repo.id = r.id where l.repo.id = (:id)")
    List<GitRepoLabel> findRepoLabels(@Param("id") Long id);
    @Query("select distinct lower(l.label) as label from GitRepoLabel l group by label order by count(label) desc")
    List<String> findAllLabels();
    void deleteAllByRepo(GitRepo repo);
}
