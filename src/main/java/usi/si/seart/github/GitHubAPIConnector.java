package usi.si.seart.github;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import graphql.GraphqlErrorException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import usi.si.seart.collection.Ranges;
import usi.si.seart.exception.github.GitHubAPIException;
import usi.si.seart.git.Commit;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubAPIConnector {

    @NonFinal
    @Value("${app.crawl.minimum-stars}")
    Integer minimumStars;

    OkHttpClient httpClient;

    GraphQlClient graphQlClient;

    RetryTemplate retryTemplate;

    Ranges.Printer<Date> rangePrinter;

    GitHubTokenManager gitHubTokenManager;

    ConversionService conversionService;

    public JsonObject searchRepositories(String language, Range<Date> range, Integer page) {
        Map<String, String> query = ImmutableMap.<String, String>builder()
                .put("language", URLEncoder.encode(language, StandardCharsets.UTF_8))
                .put("pushed", rangePrinter.print(range))
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

        FetchCallback.Result result = fetch(url);
        return result.getJsonObject();
    }

    public JsonObject fetchRepoInfo(String name) {
        GraphQLCallback.Result result = fetch(name);
        return result.getJsonObject();
    }

    public Commit fetchLastCommitInfo(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_COMMITS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        FetchCallback.Result result = fetch(url);
        JsonArray commits = result.getJsonArray();
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

    public Long fetchNumberOfCommits(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_COMMITS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfBranches(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_BRANCHES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfReleases(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_RELEASES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfContributors(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_CONTRIBUTORS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfOpenIssuesAndPulls(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_ISSUES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "open")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfAllIssuesAndPulls(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_ISSUES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "all")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfOpenPulls(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_PULLS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "open")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfAllPulls(String name) {
        
        URL url = HttpUrl.get(Endpoint.REPOSITORY_PULLS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .addQueryParameter("state", "all")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    private Long fetchLastPageNumberFromHeader(URL url) {
        FetchCallback.Result result = fetch(url);
        if (result.getStatus() == HttpStatus.FORBIDDEN) {
            /*
             * Response status code 403, two possibilities:
             * (1) The rate limit for the current token is exceeded
             * (2) The request is too expensive for GitHub to compute
             * (e.g. https://api.github.com/repos/torvalds/linux/contributors)
             *
             * Since we make use of guards for the former case,
             * then the latter is always the response cause.
             * As a result we return null value to denote the metric as unobtainable.
             */
            return null;
        } else {
            Headers headers = result.getHeaders();
            String link = headers.get("link");
            if (link != null) {
                NavigationLinks links = conversionService.convert(link, NavigationLinks.class);
                return links.getLastPage();
            } else {
                return result.size().map(Integer::longValue).orElse(1L);
            }
        }
    }

    public JsonArray fetchRepoLabels(String name) {
        JsonArray array = new JsonArray();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LABELS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            FetchCallback.Result result = fetch(url);
            array.addAll(result.getJsonArray());
            Headers headers = result.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link).map(str -> {
                NavigationLinks links = conversionService.convert(str, NavigationLinks.class);
                return links.getNext();
            }).orElse(null);
        } while (url != null);
        return array;
    }

    public JsonObject fetchRepoLanguages(String name) {
        JsonObject object = new JsonObject();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LANGUAGES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            FetchCallback.Result result = fetch(url);
            result.getJsonObject().entrySet().forEach(entry -> object.add(entry.getKey(), entry.getValue()));
            Headers headers = result.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link).map(str -> {
                NavigationLinks links = conversionService.convert(str, NavigationLinks.class);
                return links.getNext();
            }).orElse(null);
        } while (url != null);
        return object;
    }

    public JsonArray fetchRepoTopics(String name) {
        JsonArray array = new JsonArray();
        URL url = HttpUrl.get(Endpoint.REPOSITORY_TOPICS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        do {
            FetchCallback.Result result = fetch(url);
            JsonObject object = result.getJsonObject();
            array.addAll(object.getAsJsonArray("names"));
            Headers headers = result.getHeaders();
            String link = headers.get("link");
            url = Optional.ofNullable(link).map(str -> {
                NavigationLinks links = conversionService.convert(str, NavigationLinks.class);
                return links.getNext();
            }).orElse(null);
        } while (url != null);
        return array;
    }

    private GraphQLCallback.Result fetch(String name) {
        String[] args = name.split("/");
        if (args.length != 2)
            throw new IllegalArgumentException("Invalid repository name: " + name);
        Map<String, Object> variables = Map.of("owner", args[0], "name", args[1]);
        try {
            return retryTemplate.execute(new GraphQLCallback(variables));
        } catch (Exception ex) {
            String message = String.format("GraphQL request to %s failed", name);
            throw new GitHubAPIException(message, ex);
        }
    }

    private FetchCallback.Result fetch(URL url) {
        try {
            return retryTemplate.execute(new FetchCallback(url));
        } catch (Exception ex) {
            String message = String.format("Request to %s failed", url);
            throw new GitHubAPIException(message, ex);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private abstract static class Result {

        JsonElement jsonElement;

        public JsonObject getJsonObject() {
            return jsonElement.getAsJsonObject();
        }

        public JsonArray getJsonArray() {
            return jsonElement.getAsJsonArray();
        }

        public Optional<Integer> size() {
            if (jsonElement.isJsonArray()) {
                return Optional.of(jsonElement.getAsJsonArray().size());
            } else if (jsonElement.isJsonObject()) {
                return Optional.of(jsonElement.getAsJsonObject().size());
            } else {
                return Optional.empty();
            }
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private class GraphQLCallback implements RetryCallback<GraphQLCallback.Result, Exception> {

        Map<String, Object> variables;

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        private class Result extends GitHubAPIConnector.Result {

            private Result(JsonElement jsonElement) {
                super(jsonElement);
            }
        }

        @Override
        public GraphQLCallback.Result doWithRetry(RetryContext context) {
            GraphQlResponse response = graphQlClient.documentName("repository")
                    .variables(variables)
                    .execute()
                    .block();
            Map<String, Object> data = response.getData();
            List<ResponseError> errors = response.getErrors();
            if (!errors.isEmpty()) {
                Collector<CharSequence, ?, String> collector = Collectors.joining(",", "[", "]");
                String messages = errors.stream().map(ResponseError::getMessage).collect(collector);
                throw GraphqlErrorException.newErrorException()
                        .message("Response returned the following errors: " + messages)
                        .build();
            }
            JsonObject raw = conversionService.convert(data, JsonObject.class);
            JsonObject repository = raw.getAsJsonObject("repository");
            return new Result(repository);
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private class FetchCallback implements RetryCallback<FetchCallback.Result, Exception> {

        URL url;

        @Getter
        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        private class Result extends GitHubAPIConnector.Result {

            HttpStatus status;
            Headers headers;

            public Result(JsonElement jsonElement, HttpStatus status, Headers headers) {
                super(jsonElement);
                this.status = status;
                this.headers = headers;
            }
        }

        @Override
        @SuppressWarnings("resource")
        public Result doWithRetry(RetryContext context) throws Exception {
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            String currentToken = gitHubTokenManager.getCurrentToken();
            if (currentToken != null)
                builder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
            Request request = builder.build();
            Response response = httpClient.newCall(request).execute();

            HttpStatus status = HttpStatus.valueOf(response.code());
            HttpStatus.Series series = status.series();
            Headers headers = response.headers();
            String body = response.body().string();
            JsonElement element = conversionService.convert(body, JsonElement.class);

            switch (series) {
                case SUCCESSFUL:
                    return new Result(element, status, headers);
                case INFORMATIONAL:
                case REDIRECTION:
                    return new Result(JsonNull.INSTANCE, status, headers);
                case CLIENT_ERROR:
                    return handleClientError(status, headers, element.getAsJsonObject());
                case SERVER_ERROR:
                    return handleServerError(status, element.getAsJsonObject());
                default:
            }

            throw new IllegalStateException("This line should never be reached");
        }

        private Result handleServerError(HttpStatus status, JsonObject json) {
            GitHubAPIConnector.log.error("Server Error: {} [{}]", status.value(), status.getReasonPhrase());
            ErrorResponse errorResponse = conversionService.convert(json, ErrorResponse.class);
            throw new HttpServerErrorException(status, errorResponse.getMessage());
        }

        @SuppressWarnings("java:S128")
        private Result handleClientError(
                HttpStatus status, Headers headers, JsonObject json
        ) throws InterruptedException {
            ErrorResponse errorResponse = conversionService.convert(json, ErrorResponse.class);
            switch (status) {
                case UNAUTHORIZED:
                    /*
                     * Here we should not call `replaceTokenIfExpired()`
                     * since it would lead to an infinite loop,
                     * because we are checking the Rate Limit API
                     * with the very same unauthorized token.
                     */
                    gitHubTokenManager.replaceToken();
                    break;
                case TOO_MANY_REQUESTS:
                    GitHubAPIConnector.log.warn("Too many requests, sleeping for 5 minutes...");
                    TimeUnit.MINUTES.sleep(5);
                    break;
                case FORBIDDEN:
                    /*
                     * Response status code 403, two possibilities:
                     * (1) The rate limit for the current token is exceeded
                     * (2) The request is too expensive for GitHub to compute
                     * (e.g. https://api.github.com/repos/torvalds/linux/contributors)
                     */
                    String header = "X-RateLimit-Remaining";
                    int remaining = Optional.ofNullable(headers.get(header))
                            .map(Integer::parseInt)
                            .orElse(-1);
                    if (remaining == -1) {
                        String template = "The '%s' header could not be found, application logic needs an update";
                        String message = String.format(template, header);
                        throw new IllegalStateException(message);
                    } else if (remaining == 0) {
                        gitHubTokenManager.replaceTokenIfExpired();
                        break;
                    } else {
                        /*
                         * Case (2) encountered, so we propagate error upwards
                         * @see fetchLastPageNumberFromHeader
                         */
                        return new Result(json, status, headers);
                    }
                default:
                    // TODO: 30.07.23 Add any other special logic here
            }
            throw new HttpClientErrorException(status, errorResponse.getMessage());
        }
    }
}
