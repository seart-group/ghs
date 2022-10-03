package usi.si.seart.gseapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Clock;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * By default, Spring Boot will use just a single thread for all scheduled tasks to run.
     * Since we have three scheduler jobs:
     *
     * <ul>
     *     <li>Crawler</li>
     *     <li>CleanUp</li>
     *     <li>CacheEvict</li>
     * </ul>
     *
     * We configure the threads here.
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setClock(Clock.systemUTC());
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("GHSThread");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}
