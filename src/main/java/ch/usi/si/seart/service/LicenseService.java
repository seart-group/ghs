package ch.usi.si.seart.service;

import ch.usi.si.seart.model.License;
import ch.usi.si.seart.repository.LicenseRepository;
import ch.usi.si.seart.repository.LicenseStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.Collection;

public interface LicenseService extends NamedEntityService<License> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LicenseServiceImpl implements LicenseService {

        LicenseRepository licenseRepository;
        LicenseStatisticsRepository licenseStatisticsRepository;

        @PersistenceContext
        EntityManager entityManager;

        @Override
        public License getOrCreate(String name) {
            return licenseRepository.findByName(name)
                    .orElseGet(() -> licenseRepository.save(
                            License.builder()
                                    .name(name)
                                    .build()
                    ));
        }

        @Override
        public Collection<License> getAll(Pageable pageable) {
            Pageable adjusted = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.Direction.DESC,
                    License.Statistics_.COUNT
            );
            return licenseStatisticsRepository.findAll(adjusted).stream()
                    .map(License.Statistics::getLicense)
                    .toList();
        }

        /*
         * This query works fine, but IntelliJ doesn't like it for some reason.
         * This comment acts as a reminder on why the inspection is suppressed.
         */
        @SuppressWarnings("JpaQlInspection")
        @Override
        public Collection<License> getByNameContains(String name, Pageable pageable) {
            TypedQuery<Tuple> query = entityManager.createQuery(
                    """
                    select license,
                    case when license.name = :seq then 0
                         when license.name like concat(:seq, '%') then 1
                         when license.name like concat('%', :seq) then 3
                         else 2
                    end as score
                    from License license
                    inner join license.statistics
                    where license.name like concat('%', :seq, '%')
                    order by
                        score,
                        license.statistics.count desc,
                        license.name
                    """,
                    Tuple.class
            );
            int limit = pageable.getPageSize();
            int offset = Math.toIntExact(pageable.getOffset());
            return query.setParameter("seq", name)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList()
                    .stream()
                    .map(this::convert)
                    .toList();
        }

        private License convert(Tuple tuple) {
            return tuple.get(0, License.class);
        }
    }
}
