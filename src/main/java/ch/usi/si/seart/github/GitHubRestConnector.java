package ch.usi.si.seart.github;

import ch.usi.si.seart.config.properties.CrawlerProperties;
import ch.usi.si.seart.exception.github.GitHubConnectorException;
import ch.usi.si.seart.exception.github.GitHubRestException;
import ch.usi.si.seart.git.Commit;
import ch.usi.si.seart.github.response.ErrorResponse;
import ch.usi.si.seart.github.response.RestResponse;
import ch.usi.si.seart.util.Ranges;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubRestConnector extends GitHubConnector<RestResponse> {

    OkHttpClient httpClient;

    GitHubTokenManager gitHubTokenManager;

    ConversionService conversionService;

    Ranges.Printer<Date> dateRangePrinter;

    Integer minimumStars;

    @Autowired
    public GitHubRestConnector(
            OkHttpClient httpClient,
            GitHubTokenManager gitHubTokenManager,
            ConversionService conversionService,
            Ranges.Printer<Date> dateRangePrinter,
            CrawlerProperties properties
    ) {
        this.httpClient = httpClient;
        this.gitHubTokenManager = gitHubTokenManager;
        this.conversionService = conversionService;
        this.dateRangePrinter = dateRangePrinter;
        this.minimumStars = properties.getMinimumStars();
    }

    @SuppressWarnings("ConstantConditions")
    public JsonObject searchRepositories(String language, Range<Date> dateRange, Integer page) {
        Map<String, String> query = ImmutableMap.<String, String>builder()
                .put("language", URLEncoder.encode("\"" + language + "\"", StandardCharsets.UTF_8))
                .put("pushed", dateRangePrinter.print(dateRange))
                .put("stars", String.format(">=%d", minimumStars))
                .put("fork", "true")
                .put("is", "public")
                .build();

        String joined = Joiner.on("+").withKeyValueSeparator(":").join(query);

        URL url = HttpUrl.get(Endpoint.SEARCH_REPOSITORIES)
                .newBuilder()
                .setEncodedQueryParameter("q", joined)
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", "100")
                .build()
                .url();

        return execute(new RestCallback(url)).getJsonObject();
    }

    public HttpStatus pingRepository(String name) {
        URL url = Endpoint.REPOSITORY.toURL(name.split("/"));
        RestCallback callback = new RestCallback(url);
        RestResponse response = execute(callback);
        return response.getStatus();
    }

    @SuppressWarnings("ConstantConditions")
    public JsonArray getRepositoryLabels(String name) {
        JsonArray array = new JsonArray();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LABELS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            RestResponse response = execute(new RestCallback(url));
            array.addAll(response.getJsonArray());
            Headers headers = response.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link)
                    .map(string -> conversionService.convert(string, NavigationLinks.class))
                    .map(NavigationLinks::getNext)
                    .orElse(null);
        } while (url != null);
        return array;
    }

    @SuppressWarnings("ConstantConditions")
    public JsonObject getRepositoryLanguages(String name) {
        JsonObject object = new JsonObject();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LANGUAGES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            RestResponse response = execute(new RestCallback(url));
            response.getJsonObject().entrySet().forEach(entry -> object.add(entry.getKey(), entry.getValue()));
            Headers headers = response.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link)
                    .map(string -> conversionService.convert(string, NavigationLinks.class))
                    .map(NavigationLinks::getNext)
                    .orElse(null);
        } while (url != null);
        return object;
    }

    @SuppressWarnings("ConstantConditions")
    public JsonArray getRepositoryTopics(String name) {
        JsonArray array = new JsonArray();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_TOPICS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            RestResponse response = execute(new RestCallback(url));
            JsonObject object = response.getJsonObject();
            array.addAll(object.getAsJsonArray("names"));
            Headers headers = response.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link)
                    .map(string -> conversionService.convert(string, NavigationLinks.class))
                    .map(NavigationLinks::getNext)
                    .orElse(null);
        } while (url != null);
        return array;
    }

    @SuppressWarnings("ConstantConditions")
    public Commit getRepositoryLastCommit(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_COMMITS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        JsonArray commits = execute(new RestCallback(url)).getJsonArray();
        try {
            JsonObject latest = commits.get(0).getAsJsonObject();
            return conversionService.convert(latest, Commit.class);
        } catch (IndexOutOfBoundsException ignored) {
            /*
             * It might be possible for a repository to have no commits.
             * However, such repositories should never appear in the search,
             * because we target repositories written in a specific language!
             * Still, better safe than sorry...
             */
            return Commit.UNKNOWN;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryCommits(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_COMMITS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryBranches(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_BRANCHES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryReleases(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_RELEASES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryContributors(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_CONTRIBUTORS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryOpenPullRequests(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_PULLS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "open")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    public Long countRepositoryTotalPullRequests(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_PULLS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "all")
                .build()
                .url();
        return getLastPageNumberFromHeader(url);
    }

    @SuppressWarnings("ConstantConditions")
    private Long getLastPageNumberFromHeader(URL url) {
        RestCallback callback = new RestCallback(url);
        RestResponse response = execute(callback);
        return switch (response.getStatus()) {
            /*
             * Error status codes 403, 404, and 451 are non-retryable, causes include:
             *
             * (1) Repository is too large to obtain the metric through the API
             * (2) Repository is private or does not exist
             * (3) Repository is unavailable for legal reasons
             *
             * As a result we return null to denote the metric as unobtainable.
             */
            case FORBIDDEN, NOT_FOUND, UNAVAILABLE_FOR_LEGAL_REASONS -> null;
            default -> {
                Headers headers = response.getHeaders();
                String link = headers.get("link");
                if (link != null) {
                    NavigationLinks links = conversionService.convert(link, NavigationLinks.class);
                    yield links.getLastPage();
                } else {
                    yield response.size().map(Integer::longValue).orElse(1L);
                }
            }
        };
    }

    @Override
    protected RestResponse execute(Callback<RestResponse> callback) {
        try {
            return super.execute(callback);
        } catch (GitHubConnectorException ex) {
            throw new GitHubRestException(ex.getCause());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private final class RestCallback implements Callback<RestResponse> {

        URL url;

        @Override
        @SuppressWarnings("resource")
        public RestResponse doWithRetry(RetryContext context) throws Exception {
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            String currentToken = gitHubTokenManager.getCurrentToken();
            if (currentToken != null)
                builder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
            Request request = builder.build();
            okhttp3.Response response = httpClient.newCall(request).execute();

            HttpStatus status = HttpStatus.valueOf(response.code());
            HttpStatus.Series series = status.series();
            Headers headers = response.headers();
            String body = response.body().string();
            JsonElement element = conversionService.convert(body, JsonElement.class);

            return switch (series) {
                case SUCCESSFUL -> new RestResponse(element, status, headers);
                case INFORMATIONAL, REDIRECTION -> new RestResponse(JsonNull.INSTANCE, status, headers);
                case CLIENT_ERROR -> handleClientError(status, headers, element.getAsJsonObject());
                case SERVER_ERROR -> handleServerError(status, element.getAsJsonObject());
            };
        }

        private RestResponse handleServerError(HttpStatus status, JsonObject json) {
            log.error("Server Error: {} [{}]", status.value(), status.getReasonPhrase());
            ErrorResponse errorResponse = conversionService.convert(json, ErrorResponse.class);
            throw new HttpServerErrorException(status, errorResponse.getMessage());
        }

        @SuppressWarnings("java:S128")
        private RestResponse handleClientError(
                HttpStatus status, Headers headers, JsonObject json
        ) throws InterruptedException {
            ErrorResponse errorResponse = conversionService.convert(json, ErrorResponse.class);
            switch (status) {
                case UNAUTHORIZED ->
                    /*
                     * Here we should not call `replaceTokenIfExpired()`
                     * since it would lead to an infinite loop,
                     * because we are checking the Rate Limit API
                     * with the very same unauthorized token.
                     */
                    gitHubTokenManager.replaceToken();
                case TOO_MANY_REQUESTS -> {
                    log.warn("Too many requests, sleeping for 5 minutes...");
                    TimeUnit.MINUTES.sleep(5);
                }
                case FORBIDDEN -> {
                    /*
                     * Response status code 403, three possibilities:
                     * (1) The rate limit for the current token is exceeded
                     * (2) The request is too expensive for GitHub to compute
                     * (e.g. https://api.github.com/repos/torvalds/linux/contributors)
                     * (3) The repository is taken down due to a TOS violation
                     * (e.g. https://api.github.com/repos/mlwrx1978/freenode/releases)
                     */
                    String header = GitHubHttpHeaders.X_RATELIMIT_REMAINING;
                    String value = headers.get(header);
                    int remaining = Optional.ofNullable(value)
                            .map(Integer::parseInt)
                            .orElse(-1);
                    if (remaining == -1) {
                        String template = "The '%s' header could not be found, application logic needs an update";
                        String message = String.format(template, header);
                        throw new IllegalStateException(message);
                    } else if (remaining == 0) {
                        gitHubTokenManager.replaceTokenIfExpired();
                    } else {
                        /*
                         * Case (2) or (3) encountered,
                         * so we propagate error upwards.
                         *
                         * @see fetchLastPageNumberFromHeader
                         */
                        return new RestResponse(json, status, headers);
                    }
                }
                case NOT_FOUND, UNAVAILABLE_FOR_LEGAL_REASONS -> {
                    /*
                     * These are non-retryable errors,
                     * and as such we propagate them upwards.
                     *
                     * In case of 404, there are two possibilities:
                     * (1) The repository does not exist
                     * (2) The repository is private
                     *
                     * In case of 451, the repository was
                     * taken down due to a DMCA violation.
                     *
                     * @see fetchLastPageNumberFromHeader
                     */
                    return new RestResponse(json, status, headers);
                }
                default -> {
                    // TODO: 30.07.23 Add any other special logic here
                }
            }
            throw new HttpClientErrorException(status, errorResponse.getMessage());
        }
    }
}
