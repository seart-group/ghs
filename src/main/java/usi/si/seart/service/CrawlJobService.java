package usi.si.seart.service;

import java.util.Date;

public interface CrawlJobService {
    Date getCrawlDateByLanguage(String language);
    void updateCrawlDateForLanguage(String language, Date date);
}
