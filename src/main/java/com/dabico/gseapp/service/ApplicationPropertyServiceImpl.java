package com.dabico.gseapp.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Getter
@Setter
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationPropertyServiceImpl implements ApplicationPropertyService {
    @Value(value = "${app.crawl.enabled}")
    Boolean enabled;

    @Value(value = "${app.crawl.scheduling}")
    Long scheduling;

    @Value("#{new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\").parse(\"${app.crawl.startdate}\")}")
    Date startDate;
}
