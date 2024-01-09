package ch.usi.si.seart.github;

import ch.usi.si.seart.collection.Cycle;
import ch.usi.si.seart.config.properties.GitHubProperties;
import ch.usi.si.seart.exception.github.GitHubTokenManagerException;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.util.Set;
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
public class GitHubTokenManager implements InitializingBean {

    OkHttpClient httpClient;

    RetryTemplate retryTemplate;

    ConversionService conversionService;

    @Getter
    @NonFinal
    String currentToken = null;

    Cycle<String> tokens;

    @Autowired
    public GitHubTokenManager(
            OkHttpClient httpClient,
            @Qualifier("timeLimitedRetryTemplate")
            RetryTemplate retryTemplate,
            ConversionService conversionService,
            GitHubProperties properties
    ) {
        this.httpClient = httpClient;
        this.retryTemplate = retryTemplate;
        this.conversionService = conversionService;
        this.tokens = new Cycle<>(properties.getTokens());
    }

    public void replaceToken() {
        if (tokens.hasNext()) {
            currentToken = tokens.next();
        }
    }

    public void replaceTokenIfExpired() {
        try {
            RateLimit rateLimit = retryTemplate.execute(new RateLimitPollCallback());
            log.debug("GitHub API:    Core {}", rateLimit.core());
            log.debug("GitHub API:  Search {}", rateLimit.search());
            log.debug("GitHub API: GraphQL {}", rateLimit.graphql());
            if (rateLimit.anyExceeded()) {
                replaceToken();
                long maxWaitSeconds = rateLimit.getMaxWaitSeconds();
                if (maxWaitSeconds > 0) {
                    String maxWaitDuration = rateLimit.getMaxWaitReadable();
                    log.info("Rate limits exhausted, sleeping for {}...", maxWaitDuration);
                    TimeUnit.SECONDS.sleep(maxWaitSeconds + 1);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GitHubTokenManagerException("Interrupted while waiting for token to replenish", ex);
        } catch (Exception ex) {
            throw new GitHubTokenManagerException("Token replacement failed", ex);
        }
    }

    @Override
    public void afterPropertiesSet() {
        GitHubTokenValidator validator = new GitHubTokenValidator();
        tokens.toSet().forEach(validator::validate);
        int size = tokens.size();
        switch (size) {
            case 0 -> {
                log.warn("Access tokens not specified, can not mine the GitHub API!");
                log.info(
                        "Generate a new access token on https://github.com/settings/tokens " +
                                "and add it to the `ghs.github.tokens` property in `ghs.properties`!"
                );
            }
            case 1 -> {
                log.info(
                        "Single token specified for GitHub API mining, " +
                                "consider adding more tokens to increase the crawler's efficiency."
                );
                currentToken = tokens.next();
            }
            default -> {
                log.info("Loaded {} tokens for usage in mining!", size);
                currentToken = tokens.next();
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class GitHubTokenValidator {

        RestTemplate template;

        GitHubTokenValidator() {
            UriTemplateHandler templateHandler = new DefaultUriBuilderFactory(Endpoint.RATE_LIMIT.toString());
            ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();
            this.template = new RestTemplateBuilder()
                    .uriTemplateHandler(templateHandler)
                    .errorHandler(errorHandler)
                    .build();
        }

        public void validate(String token) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = template.exchange("", HttpMethod.GET, entity, String.class);
            headers = response.getHeaders();
            String value = headers.getFirst(GitHubHttpHeaders.X_OAUTH_SCOPES);
            Assert.notNull(value, "Token does not have any scopes!");
            Set<String> scopes = Set.of(value.split(","));
            Assert.isTrue(scopes.contains("repo"), "Token does not have the `repo` scope!");
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
            Response response = httpClient.newCall(request).execute();
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
