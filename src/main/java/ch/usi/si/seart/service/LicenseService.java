package ch.usi.si.seart.service;

import ch.usi.si.seart.model.License;
import ch.usi.si.seart.repository.LicenseRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;

public interface LicenseService extends NamedEntityService<License> {

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LicenseServiceImpl implements LicenseService {

        LicenseRepository licenseRepository;

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
            return licenseRepository.findAll(pageable).getContent();
        }
    }
}
