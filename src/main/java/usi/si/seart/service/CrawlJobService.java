package usi.si.seart.service;

import usi.si.seart.model.CrawlJob;

import java.util.Date;
import java.util.List;

public interface CrawlJobService {
    List<CrawlJob> getCompletedJobs();
    Date getCrawlDateByLanguage(String language);
    void updateCrawlDateForLanguage(String language, Date date);
}
