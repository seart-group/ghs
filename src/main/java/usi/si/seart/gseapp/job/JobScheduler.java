package usi.si.seart.gseapp.job;

import lombok.extern.slf4j.Slf4j;
import usi.si.seart.gseapp.db_access_service.ApplicationPropertyService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
@EnableScheduling
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobScheduler {

    CrawlProjectsJob crawlProjectsJob;
    ApplicationPropertyService applicationPropertyService;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob,ApplicationPropertyService applicationPropertyService){
        this.crawlProjectsJob = crawlProjectsJob;
        this.applicationPropertyService = applicationPropertyService;
    }

    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getCrawlScheduling()}")
    public void run(){
        try {
            if(crawlProjectsJob.running) {
                // Emad: It seems the @Scheduled run this method no matters if existing run is finished or not. Not sure.
                log.info("Next crawl job postponed due to an on-going job");
                return;
            }
            crawlProjectsJob.run();
            log.info("Next crawl scheduled for: " + Date.from(Instant.now().plusMillis(applicationPropertyService.getCrawlScheduling())));
        } catch (Exception ex) {
            crawlProjectsJob.running = false;
            ex.printStackTrace();
        }
    }
}
