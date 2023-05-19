package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.GitRepoTopicKey;

import java.util.Set;

public interface GitRepoTopicRepository extends JpaRepository<GitRepoTopic, GitRepoTopicKey> {

    Set<GitRepoTopic> findAllByRepo(GitRepo repo);
}