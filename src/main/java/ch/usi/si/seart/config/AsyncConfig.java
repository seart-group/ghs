package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.AnalysisProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "analysisExecutor")
    public Executor executor(AnalysisProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setMaxPoolSize(properties.getMaxPoolThreads());
        executor.setThreadNamePrefix("analysis-");
        executor.setQueueCapacity(128);
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncUncaughtExceptionHandler asyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {

            private final Logger log = LoggerFactory.getLogger(
                    AsyncConfig.class.getCanonicalName() + "$" + AsyncUncaughtExceptionHandler.class.getSimpleName()
            );

            @Override
            public void handleUncaughtException(
                    @NotNull Throwable throwable, @NotNull Method method, @NotNull Object... params
            ) {
                log.error("Unhandled exception occurred while executing asynchronous method: {}", method, throwable);
            }
        };
    }
}
