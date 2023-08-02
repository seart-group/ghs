package usi.si.seart.config;

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

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryListener retryListener() {
        return new RetryListener() {

            private final Logger log = LoggerFactory.getLogger("usi.si.seart.config.RetryListener$RetryListener");

            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable
            ) {
            }

            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable
            ) {
                String template = "Operation failed, retry attempt: %d";
                String message = String.format(template, context.getRetryCount());
                log.warn(message, throwable);
            }
        };
    }

    @Bean
    @Primary
    public RetryTemplate attemptLimitedRetryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(5)
                .customBackoff(backOffPolicy())
                .retryOn(Exception.class)
                .withListener(retryListener())
                .build();
    }

    @Bean
    public RetryTemplate timeLimitedRetryTemplate() {
        return RetryTemplate.builder()
                .withTimeout(7_200_000)
                .customBackoff(backOffPolicy())
                .retryOn(Exception.class)
                .withListener(retryListener())
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
