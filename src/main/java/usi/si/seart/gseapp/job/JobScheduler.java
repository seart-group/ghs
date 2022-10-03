package usi.si.seart.gseapp.job;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import usi.si.seart.gseapp.db_access_service.ApplicationPropertyService;

import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class JobScheduler {

    CrawlProjectsJob crawlProjectsJob;
    ApplicationPropertyService applicationPropertyService;

    @Scheduled(fixedRateString = "#{@applicationPropertyServiceImpl.getCrawlScheduling()}")
    @ConditionalOnProperty(value = "app.crawl.enabled", havingValue = "true")
    public void run(){
        try {
            if (crawlProjectsJob.running) {
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
