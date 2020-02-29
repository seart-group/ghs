package com.dabico.gseapp.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class JobScheduler {

    static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private CrawlProjectsJob crawlProjectsJob;

    @Autowired
    public JobScheduler(CrawlProjectsJob crawlProjectsJob){
        this.crawlProjectsJob = crawlProjectsJob;
    }

    @Scheduled(fixedRateString = "31556952000")
    public void run(){
        crawlProjectsJob.run();
    }
}
