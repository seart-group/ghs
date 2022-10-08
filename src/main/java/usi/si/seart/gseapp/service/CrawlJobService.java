package usi.si.seart.gseapp.service;

import usi.si.seart.gseapp.model.CrawlJob;

import java.util.Date;
import java.util.List;

public interface CrawlJobService {
    List<CrawlJob> getCompletedJobs();
    Date getCrawlDateByLanguage(String language);
    void updateCrawlDateForLanguage(String language, Date date);
}
