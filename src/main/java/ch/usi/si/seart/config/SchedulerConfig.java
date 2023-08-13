package ch.usi.si.seart.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import java.time.Clock;

@Configuration
@EnableScheduling
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerConfig {

    HikariDataSource hikariDataSource;
    ApplicationContext applicationContext;

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
        threadPoolTaskScheduler.setErrorHandler(errorHandler());
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ErrorHandler() {

            private final Logger log = LoggerFactory.getLogger("usi.si.seart.config.SchedulerConfig$ErrorHandler");

            @Override
            public void handleError(@NotNull Throwable t) {
                if (t instanceof OutOfMemoryError ex) {
                    handleError(ex);
                } else if (t instanceof NonTransientDataAccessException ex) {
                    handleError(ex);
                } else {
                    log.error("Unhandled exception occurred while performing a scheduled job.", t);
                }
            }

            private void handleError(OutOfMemoryError ex) {
                shutdown("Application has run out of memory!", ex);
            }

            private void handleError(NonTransientDataAccessException ex) {
                shutdown("Non-transient exception occurred!", ex);
            }

            private void shutdown(String message, Throwable cause) {
                log.error(message, cause);
                shutdown();
            }

            private void shutdown() {
                log.error("Commencing shutdown...");
                hikariDataSource.close();
                int code = SpringApplication.exit(applicationContext, () -> 1);
                System.exit(code);
            }
        };
    }
}

