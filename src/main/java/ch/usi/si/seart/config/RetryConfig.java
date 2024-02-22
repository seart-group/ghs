package ch.usi.si.seart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryListener retryListener() {
        return new RetryListener() {

            private final Logger log = LoggerFactory.getLogger(
                    RetryConfig.class.getCanonicalName() + "$" + RetryListener.class.getSimpleName()
            );

            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable
            ) {
                log.warn(
                        "Operation failed [{}], retry attempt: {}",
                        throwable.getClass().getSimpleName(),
                        context.getRetryCount()
                );
                log.debug("", throwable);
            }
        };
    }

    @Bean
    @Primary
    public RetryTemplate attemptLimitedRetryTemplate(BackOffPolicy backOffPolicy) {
        return RetryTemplate.builder()
                .maxAttempts(5)
                .customBackoff(backOffPolicy)
                .retryOn(Exception.class)
                .build();
    }

    @Bean
    public RetryTemplate timeLimitedRetryTemplate(BackOffPolicy backOffPolicy) {
        return RetryTemplate.builder()
                .withTimeout(Duration.ofHours(2))
                .customBackoff(backOffPolicy)
                .retryOn(Exception.class)
                .build();
    }

    @Bean
    public RetryTemplate noRetryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(1)
                .noBackoff()
                .build();
    }

    @Bean
    public BackOffPolicy backOffPolicy() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1_250);
        backOffPolicy.setMaxInterval(30_000);
        backOffPolicy.setMultiplier(2);
        return backOffPolicy;
    }
}
