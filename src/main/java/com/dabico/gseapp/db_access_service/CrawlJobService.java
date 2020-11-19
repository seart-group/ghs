package com.dabico.gseapp.db_access_service;

import com.dabico.gseapp.dto.CrawlJobDtoList;

import java.util.Date;

public interface CrawlJobService {
    CrawlJobDtoList getCompletedJobs();
    Date getCrawlDateByLanguage(String language);
    void updateCrawlDateForLanguage(String language, Date date);
}
