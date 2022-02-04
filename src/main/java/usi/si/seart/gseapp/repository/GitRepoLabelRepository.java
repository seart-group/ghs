package usi.si.seart.gseapp.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;

import java.util.List;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel, Long> {
    @Query("select distinct lower(l.label) as label from GitRepoLabel l group by label order by count(label) desc")
    @Cacheable(value = "labels")
    List<String> findAllLabels();

    void deleteAllByRepo(GitRepo repo);
}
