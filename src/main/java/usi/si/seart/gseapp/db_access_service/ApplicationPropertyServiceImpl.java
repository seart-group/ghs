package usi.si.seart.gseapp.db_access_service;

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
    Long crawlScheduling;

    @Value(value = "${app.cleanup.scheduling}")
    Long cleanUpScheduling;

    @Value("#{new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\").parse(\"${app.crawl.startdate}\")}")
    Date startDate;

    @Value(value = "${app.search.page-size}")
    Integer pageSize;
}
