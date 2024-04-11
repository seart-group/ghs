package ch.usi.si.seart.service;

import ch.usi.si.seart.model.License;
import ch.usi.si.seart.repository.LicenseRepository;
import ch.usi.si.seart.repository.LicenseStatisticsRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

public interface LicenseService extends NamedEntityService<License> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LicenseServiceImpl implements LicenseService {

        LicenseRepository licenseRepository;
        LicenseStatisticsRepository licenseStatisticsRepository;

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
        public Page<License> getAll(Pageable pageable) {
            return licenseStatisticsRepository.findAll(
                    PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize(),
                            Sort.Direction.DESC,
                            License.Statistics_.COUNT
                    )
            ).map(License.Statistics::getLicense);
        }

        @Override
        public Page<License> getByNameContains(String name, Pageable pageable) {
            return licenseRepository.findAllByNameContainsOrderByBestMatch(
                    name, PageRequest.of(
                            pageable.getPageNumber(),
                            pageable.getPageSize()
                    )
            );
        }
    }
}
