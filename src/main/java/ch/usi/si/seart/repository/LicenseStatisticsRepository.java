package ch.usi.si.seart.repository;

import ch.usi.si.seart.model.License;
import ch.usi.si.seart.repository.support.ReadOnlyRepository;

public interface LicenseStatisticsRepository extends ReadOnlyRepository<License.Statistics, Long> {
}
