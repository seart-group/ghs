package ch.usi.si.seart.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "AnalysisExecutor")
    public Executor executor(@Value("${app.analysis.max-pool-threads}") int poolSize) {
        ThreadPoolTaskExecutor executor = new AnalysisThreadPoolExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AnalysisThread");
        executor.setRejectedExecutionHandler(rejectedExecutionHandler());
        executor.initialize();
        return executor;
    }

    @Bean
    public RejectedExecutionHandler rejectedExecutionHandler() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {

            private final Logger log = LoggerFactory.getLogger(
                    "usi.si.seart.config.AsyncConfig$AsyncUncaughtExceptionHandler"
            );

            @Override
            public void handleUncaughtException(
                    @NotNull Throwable throwable, @NotNull Method method, @NotNull Object... params
            ) {
                log.error("Unhandled exception occurred while executing asynchronous method: {}", method, throwable);
            }
        };
    }

    private static class AnalysisThreadPoolExecutor extends ThreadPoolTaskExecutor {

        /**
         * Overrides the default cloning thread name generation.
         * Allows for the reuse of thread names of dead cloning threads.
         */
        @NotNull
        @Override
        protected String nextThreadName() {
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);
            Set<String> names = Arrays.stream(threads)
                    .map(Thread::getName)
                    .collect(Collectors.toSet());

            return Stream.iterate(1, i -> i + 1)
                    .map(i -> getThreadNamePrefix() + i)
                    .filter(name -> !names.contains(name))
                    .findFirst()
                    .orElseGet(super::nextThreadName);
        }
    }
}
