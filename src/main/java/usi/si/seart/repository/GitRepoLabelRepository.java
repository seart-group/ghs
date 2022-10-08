package usi.si.seart.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usi.si.seart.model.GitRepoLabel;

import java.util.List;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel, Long> {

    @Query(value = "select distinct lower(l.label) as label from GitRepoLabel l group by label order by count(label) desc")
    @Cacheable(value = "labels")
    List<String> findMostFrequentLabels(Pageable pageable);
}
