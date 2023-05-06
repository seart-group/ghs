package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.GitRepoTopicKey;

public interface GitRepoTopicRepository extends JpaRepository<GitRepoTopic, GitRepoTopicKey> {

    void deleteAllByRepo(GitRepo repo);
}