package ch.usi.si.seart.service;

import ch.usi.si.seart.model.view.License;
import ch.usi.si.seart.repository.LicenseRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

public interface LicenseService {

    Collection<License> getAll();

    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(onConstructor_ = @Autowired)
    class LicenseServiceImpl implements LicenseService {

        LicenseRepository licenseRepository;

        @Override
        public Collection<License> getAll() {
            return licenseRepository.findAll();
        }
    }
}
