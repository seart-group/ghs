package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.Topic;
import ch.usi.si.seart.repository.support.ReadOnlyRepository;

public interface TopicStatisticsRepository extends ReadOnlyRepository<Topic.Statistics, Long> {
}
