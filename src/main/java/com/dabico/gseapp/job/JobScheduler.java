package com.dabico.gseapp.job;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@PropertySource("classpath:application.properties")
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobScheduler {

    @Value(value = "${app.crawl.interval}")
    String interval;

    CrawlProjectsJob crawlProjectsJob;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob){
        this.crawlProjectsJob = crawlProjectsJob;
    }

    @Scheduled(fixedRateString = "${app.crawl.scheduling}")
    public void run(){
        try {
            crawlProjectsJob.run(applicationPropertyService.getInterval(),null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
