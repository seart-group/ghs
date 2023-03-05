package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.GitRepoMetric;

public interface GitRepoMetricRepository extends JpaRepository<GitRepoMetric, Long> {

}
