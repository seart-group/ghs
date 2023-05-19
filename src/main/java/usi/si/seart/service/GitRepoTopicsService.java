package usi.si.seart.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import usi.si.seart.model.GitRepo;
import usi.si.seart.model.GitRepoTopic;
import usi.si.seart.model.Topic;
import usi.si.seart.repository.GitRepoTopicRepository;
import usi.si.seart.repository.TopicRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public interface GitRepoTopicsService {

    List<String> getAllTopicsSortByPopularity();

    Topic getOrCreateTopic(@NotNull String languageName);

    void createOrUpdateGitRepoTopics(@NotNull GitRepo repo, @NotNull List<GitRepoTopic> topics);

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class GitRepoTopicsServiceImpl implements GitRepoTopicsService {

        TopicRepository topicRepository;

        GitRepoTopicRepository gitRepoTopicRepository;

        @Override
        public List<String> getAllTopicsSortByPopularity() {
            return topicRepository.findAllSortByPopularity(PageRequest.of(0,500)).stream()
                    .map(Topic::getLabel)
                    .collect(Collectors.toList());
        }

        @Override
        public Topic getOrCreateTopic(@NotNull String label) {
            return topicRepository.findByLabel(label).orElseGet(() ->
                    topicRepository.save(
                                Topic.builder()
                                        .label(label)
                                        .build()));
        }

        @Override
        public void createOrUpdateGitRepoTopics(GitRepo repo, List<GitRepoTopic> topics) {
            gitRepoTopicRepository.deleteAllByRepo(repo);
            gitRepoTopicRepository.saveAll(topics);
        }
    }
}
