package com.dabico.gseapp.service;

import com.dabico.gseapp.util.PropertiesExtractor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Getter
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService {
    private static PropertiesExtractor propertiesExtractor = new PropertiesExtractor();

    @Setter
    @Value(value = "${app.crawl.enabled}")
    Boolean enabled;

    @Setter
    @Value(value = "${app.crawl.scheduling}")
    Long scheduling;

    @Setter
    Date startDate = PropertiesExtractor.getStartDate();
}
