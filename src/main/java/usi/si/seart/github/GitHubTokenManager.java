package usi.si.seart.github;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import usi.si.seart.collection.Cycle;
import usi.si.seart.exception.github.GitHubTokenManagerException;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    OkHttpClient client;

    RetryTemplate retryTemplate;

    ConversionService conversionService;

    @Getter
    @NonFinal
    String currentToken = null;

    Cycle<String> tokens;

    @Autowired
    public GitHubTokenManager(
            OkHttpClient client,
            @Qualifier("timeLimitedRetryTemplate")
            RetryTemplate retryTemplate,
            ConversionService conversionService,
            @Value("${app.crawl.tokens}")
            List<String> tokens
    ) {
        this.client = client;
        this.retryTemplate = retryTemplate;
        this.conversionService = conversionService;
        this.tokens = new Cycle<>(tokens);
    }

    @PostConstruct
    void postConstruct() {
        int size = tokens.size();
        switch (size) {
            case 0:
                log.warn("Access tokens not specified, can not mine the GitHub API!");
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

    public void replaceTokenIfExpired() {
        try {
            RateLimit rateLimit = retryTemplate.execute(new RateLimitPollCallback());
            log.debug("GitHub API:    Core {}", rateLimit.getCore());
            log.debug("GitHub API:  Search {}", rateLimit.getSearch());
            log.debug("GitHub API: GraphQL {}", rateLimit.getGraphql());
            if (rateLimit.anyExceeded()) {
                long waitSeconds = rateLimit.getMaxWaitSeconds();
                if (tokens.size() > 1) {
                    currentToken = tokens.next();
                } else if (waitSeconds > 0) {
                    log.info("Rate limits exhausted, sleeping for {}...", rateLimit.getMaxWaitReadable());
                    TimeUnit.SECONDS.sleep(waitSeconds + 1);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GitHubTokenManagerException("Interrupted while waiting for token to replenish", ex);
        } catch (Exception ex) {
            throw new GitHubTokenManagerException("Token replacement failed", ex);
        }
    }

    private class RateLimitPollCallback implements RetryCallback<RateLimit, Exception> {

        @Override
        @SuppressWarnings({"ConstantConditions", "resource"})
        public RateLimit doWithRetry(RetryContext context) throws Exception {
            Request.Builder builder = new Request.Builder();
            builder.url(Endpoint.RATE_LIMIT);
            if (currentToken != null)
                builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            int code = response.code();
            HttpStatus status = HttpStatus.valueOf(code);
            String phrase = status.getReasonPhrase();
            if (status.is4xxClientError()) {
                GitHubTokenManager.log.error("Client Error: {} [{}]", code, phrase);
                throw new HttpClientErrorException(status);
            }
            if (status.is5xxServerError()) {
                GitHubTokenManager.log.error("Server Error: {} [{}]", code, phrase);
                throw new HttpServerErrorException(status);
            }
            String body = response.body().string();
            JsonObject json = conversionService.convert(body, JsonObject.class);
            return conversionService.convert(json, RateLimit.class);
        }
    }
}
