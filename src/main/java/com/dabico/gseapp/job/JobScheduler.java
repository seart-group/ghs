package com.dabico.gseapp.job;

import com.dabico.gseapp.service.ApplicationPropertyService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Configuration
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobScheduler {

    CrawlProjectsJob crawlProjectsJob;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob, ApplicationPropertyService applicationPropertyService){
        this.crawlProjectsJob = crawlProjectsJob;
        this.applicationPropertyService = applicationPropertyService;
    }

    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getScheduling()}")
    public void run(){
        try {
            applicationPropertyService.setNextCrawl(new Date());
            crawlProjectsJob.run(applicationPropertyService.getInterval(),null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
