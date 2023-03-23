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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulerConfig {

    @Value("${app.crawl.cloning.maxpoolthreads}")
    int maxPoolThreads;


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

    /**
     * Configuration for the thread pool responsible for code analysis and repository cloning.
     *  No threads are instantiated when the pool is idle.
     *  Idle threads die after 60 seconds.
     *  The maximum pool size is configurable through the application.properties
     */
    @Bean(name = "GitCloning")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new GitCloningThreadPoolExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(maxPoolThreads);
        executor.setQueueCapacity(10); // maximum number of tasks in the queue, after which more threads would be created
        executor.setKeepAliveSeconds(60); // keep-alive time for idle threads
        executor.setThreadNamePrefix("CloningThread");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // policy to abort
        executor.initialize();
        return executor;
    }
}

class GitCloningThreadPoolExecutor extends ThreadPoolTaskExecutor {

    @NotNull
    @Override
    protected String nextThreadName() {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int numThreads = currentGroup.activeCount();
        Thread[] threads = new Thread[numThreads];
        currentGroup.enumerate(threads);

        for(int i=1; i<=getMaxPoolSize();i++) {
            int finalI = i;
            if(Arrays.stream(threads).noneMatch((Thread t)-> Objects.equals(t.getName(), getThreadNamePrefix() + finalI))) {
                return getThreadNamePrefix()+i;
            }
        }

        return super.nextThreadName();
    }
}