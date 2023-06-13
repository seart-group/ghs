package usi.si.seart.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.time.Clock;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableScheduling
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulerConfig {

    @Value("${app.crawl.analysis.max-pool-threads}")
    int maxPoolThreads;

    /**
     * By default, Spring Boot will use just a single thread for all scheduled tasks to run.
     * Since we have four scheduler jobs:
     *
     * <ul>
     *     <li>Crawler</li>
     *     <li>CleanUp</li>
     *     <li>CodeAnalysis</li>
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

    /**
     * Configuration for the thread pool responsible for code analysis.
     *  No threads are instantiated when the pool is idle.
     *  Idle threads die after 60 seconds.
     *  The maximum pool size is configurable through the application.properties
     */
    @Bean(name = "GitCloning")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new GitCloningThreadPoolExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(maxPoolThreads);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AnalysisThread");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    static class GitCloningThreadPoolExecutor extends ThreadPoolTaskExecutor {


        /**
         * Overrides the default cloning thread name generation.
         * Allows for the reuse of thread names of dead cloning threads.
         */
        @NotNull
        @Override
        protected String nextThreadName() {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            Thread[] th = new Thread[currentGroup.activeCount()];
            currentGroup.enumerate(th);
            Set<String> threadNames = Arrays.stream(th)
                    .map(Thread::getName)
                    .collect(Collectors.toSet());

            return Stream.iterate(1, i -> i + 1)
                    .map(i -> getThreadNamePrefix() + i)
                    .filter(name -> !threadNames.contains(name))
                    .findFirst()
                    .orElseGet(super::nextThreadName);
        }
    }
}

