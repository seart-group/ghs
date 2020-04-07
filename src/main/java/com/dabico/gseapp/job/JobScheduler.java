package com.dabico.gseapp.job;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobScheduler {

    CrawlProjectsJob crawlProjectsJob;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob){
        this.crawlProjectsJob = crawlProjectsJob;
    }

    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getScheduling()}")
    public void run(){
        try {
            crawlProjectsJob.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
