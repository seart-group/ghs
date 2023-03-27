package usi.si.seart.github;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.itning.retry.RetryException;
import io.github.itning.retry.Retryer;
import io.github.itning.retry.RetryerBuilder;
import io.github.itning.retry.strategy.stop.StopStrategies;
import io.github.itning.retry.strategy.stop.StopStrategy;
import io.github.itning.retry.strategy.wait.WaitStrategies;
import io.github.itning.retry.strategy.wait.WaitStrategy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import usi.si.seart.collection.Cycle;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Responsible for managing personal access tokens (PATs)
 * for GitHub API calls made by other components and services.
 * It contains methods for replacing tokens,
 * and checking if the currently used one has expired.
 */
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubTokenManager {

    private static final WaitStrategy RETRY_WAIT_STRATEGY = WaitStrategies.exponentialWait(100, 5, TimeUnit.MINUTES);
    private static final StopStrategy RETRY_STOP_STRATEGY = StopStrategies.stopAfterDelay(3, TimeUnit.HOURS);
    private static final Predicate<Response> RETRY_RESULT_PREDICATE = response -> !response.isSuccessful();
    private static final Class<Exception> RETRY_EXCEPTION_SUPERCLASS = Exception.class;

    OkHttpClient client;

    ConversionService conversionService;

    @Getter
    @NonFinal
    String currentToken = null;

    Cycle<String> tokens;

    @Autowired
    public GitHubTokenManager(
            OkHttpClient client,
            ConversionService conversionService,
            @Value("${app.crawl.tokens}")
            List<String> tokens
    ) {
        this.client = client;
        this.conversionService = conversionService;
        this.tokens = new Cycle<>(tokens);
    }

    @PostConstruct
    void postConstruct() {
        int size = tokens.getSize();
        switch (size) {
            case 0:
                log.warn("Access tokens not specified, GitHub API mining will be performed at a much slower rate!");
                log.info(
                        "Generate a new access token on https://github.com/settings/tokens " +
                        "and add it to the `app.crawl.tokens` property in `application.properties`!"
                );
                break;
            case 1:
                log.info(
                        "Single token specified for GitHub API mining, " +
                        "consider adding more tokens to increase the crawler's efficiency."
                );
                currentToken = tokens.next();
                break;
            default:
                log.info("Loaded {} tokens for usage in mining!", size);
                currentToken = tokens.next();
        }
    }

    public void replaceToken() {
        if (tokens.hasNext()) {
            currentToken = tokens.next();
        }
    }

    @SuppressWarnings({ "ConstantConditions", "resource" })
    @SneakyThrows({
            IOException.class,
            InterruptedException.class,
            RetryException.class,
            ExecutionException.class
    })
    public void replaceTokenIfExpired() {
        URL target = Endpoint.RATE_LIMIT.toURL();
        Request.Builder builder = new Request.Builder();
        builder.url(target);
        if (currentToken != null)
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
        Request request = builder.build();

        Callable<Response> callable = () -> client.newCall(request).execute();
        Retryer<Response> retryer = RetryerBuilder.<Response>newBuilder()
                .retryIfResult(RETRY_RESULT_PREDICATE)
                .retryIfExceptionOfType(RETRY_EXCEPTION_SUPERCLASS)
                .withWaitStrategy(RETRY_WAIT_STRATEGY)
                .withStopStrategy(RETRY_STOP_STRATEGY)
                .build();

        Response response = retryer.call(callable);
        JsonObject body = JsonParser.parseString(response.body().string()).getAsJsonObject();
        RateLimit rateLimit = conversionService.convert(body, RateLimit.class);
        if (rateLimit.anyExceeded()) {
            log.debug("GitHub API:   Core {}", rateLimit.getCoreResource());
            log.debug("GitHub API: Search {}", rateLimit.getSearchResource());
            replaceToken();
            long maxWaitSeconds = rateLimit.getMaxWaitSeconds();
            if (maxWaitSeconds > 0) {
                String maxWaitDuration = rateLimit.getMaxWaitReadable();
                log.info("Rate limits exhausted, sleeping for {}...", maxWaitDuration);
                TimeUnit.SECONDS.sleep(maxWaitSeconds + 1);
            }
        }
    }
}
