package usi.si.seart.gseapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.gseapp.model.GitRepo;
import usi.si.seart.gseapp.model.GitRepoLabel;

public interface GitRepoLabelRepository extends JpaRepository<GitRepoLabel, Long> {
    void deleteAllByRepo(GitRepo repo);
}
