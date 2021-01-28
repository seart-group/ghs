package usi.si.seart.gseapp.db_access_service;

import usi.si.seart.gseapp.dto.CrawlJobDtoList;

import java.util.Date;

public interface CrawlJobService {
    CrawlJobDtoList getCompletedJobs();
    Date getCrawlDateByLanguage(String language);
    void updateCrawlDateForLanguage(String language, Date date);
}
