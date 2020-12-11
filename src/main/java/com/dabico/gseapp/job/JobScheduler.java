package com.dabico.gseapp.job;

import com.dabico.gseapp.db_access_service.ApplicationPropertyService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Date;

@Configuration
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobScheduler {

    static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    CrawlProjectsJob crawlProjectsJob;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob,ApplicationPropertyService applicationPropertyService){
        this.crawlProjectsJob = crawlProjectsJob;
        this.applicationPropertyService = applicationPropertyService;
    }

    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getScheduling()}")
    public void run(){
        try {
            if(crawlProjectsJob.running) {
                // Emad: It seems the @Scheduled run this method no matters if existing run is finished or not. Not sure.
                logger.info("Next crawl job postponed due to an on-going job");
                return;
            }
            crawlProjectsJob.run();
            logger.info("Next crawl scheduled for: " + Date.from(Instant.now().plusMillis(applicationPropertyService.getScheduling())));
        } catch (Exception ex) {
            crawlProjectsJob.running = false;
            ex.printStackTrace();
        }
    }
}
