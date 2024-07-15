package ch.usi.si.seart.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(ErrorHandler errorHandler) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setClock(Clock.systemUTC());
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("task-");
        threadPoolTaskScheduler.setErrorHandler(errorHandler);
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean
    public ErrorHandler errorHandler(DataSource dataSource, ApplicationContext applicationContext) {
        return new ErrorHandler() {

            private static final Logger log = LoggerFactory.getLogger(
                    SchedulerConfig.class.getCanonicalName() + "$" + ErrorHandler.class.getSimpleName()
            );

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
                try {
                    log.error("Commencing shutdown...");
                    if (dataSource instanceof Closeable closeable) closeable.close();
                    int code = SpringApplication.exit(applicationContext, () -> 1);
                    System.exit(code);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }
}

