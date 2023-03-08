package usi.si.seart.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {

    /**
     * By default, Spring Boot will use just a single thread for all scheduled tasks to run.
     * Since we have three scheduler jobs:
     *
     * <ul>
     *     <li>Crawler</li>
     *     <li>CleanUp</li>
     *     <li>CacheEvict</li>
     *     <li>CodeAnalysis</li>
     * </ul>
     *
     * We configure the threads here.
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setClock(Clock.systemUTC());
        threadPoolTaskScheduler.setPoolSize(4);
        threadPoolTaskScheduler.setThreadNamePrefix("GHSThread");
        threadPoolTaskScheduler.setErrorHandler(new SchedulerErrorHandler());
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    private static class SchedulerErrorHandler implements ErrorHandler {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        public void handleError(@NotNull Throwable t) {
            log.error("Unhandled exception occurred while performing a scheduled job.", t);
        }
    }



    @Bean(name = "GitCloning")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(1000); // maximum number of tasks in the queue
        executor.setKeepAliveSeconds(60); // keep-alive time for idle threads
        executor.setThreadNamePrefix("CloningThread");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // policy to abort
        executor.initialize();
        return executor;
    }
}
