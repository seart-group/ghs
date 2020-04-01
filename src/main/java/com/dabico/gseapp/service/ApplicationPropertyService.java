package com.dabico.gseapp.service;

import com.dabico.gseapp.util.interval.DateInterval;

import java.util.Date;

public interface ApplicationPropertyService {
    Boolean getEnabled();
    void setEnabled(Boolean value);
    Long getScheduling();
    void setScheduling(Long value);
    DateInterval getInterval();
    void setInterval(DateInterval value);
    Date getNextCrawl();
    void setNextCrawl(Date value);
}
