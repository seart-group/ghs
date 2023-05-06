package usi.si.seart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.GitRepoTopicKey;
import usi.si.seart.model.Topic;

import java.util.Optional;

public interface GitRepoTopicRepository extends JpaRepository<GitRepoTopic, GitRepoTopicKey> {

    Optional<GitRepoTopic> findByTopicAndRepo(Topic topic, GitRepo repo);
}