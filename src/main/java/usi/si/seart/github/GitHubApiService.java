package usi.si.seart.github;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import usi.si.seart.util.Ranges;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubApiService {

    private static final int MIN_STARS = 10;

    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final int RETRY_SLEEP_DURATION = 1;
    private static final TimeUnit RETRY_SLEEP_TIME_UNIT = TimeUnit.MINUTES;

    /*
     * Pattern for matching Link header values of GitHub API responses.
     * https://www.debuggex.com/r/A5_ziqVy-vFaesKK
     */
    private static final Pattern HEADER_LINK_PATTERN = Pattern.compile("(?:,\\s)?<([^>]+)>;\\srel=\"(\\w+)\"");

    OkHttpClient client;
    DateFormat utcTimestampFormat;

    GitHubTokenManager gitHubTokenManager;

    ConversionService conversionService;

    @SneakyThrows(InterruptedException.class)
    private static void waitBeforeRetry() {
        RETRY_SLEEP_TIME_UNIT.sleep(RETRY_SLEEP_DURATION);
    }

    @SneakyThrows(InterruptedException.class)
    public JsonObject searchRepositories(String language, Range<Date> dateRange, Integer page, boolean crawlUpdatedRepos) {
        Map<String, String> query = ImmutableMap.<String, String>builder()
                .put("language", URLEncoder.encode(language, StandardCharsets.UTF_8))
                .put(crawlUpdatedRepos ? "pushed" : "created", Ranges.toString(dateRange, utcTimestampFormat))
                .put("stars", String.format(">=%d", MIN_STARS))
                .put("fork", "true")
                .put("is", "public")
                .build();

        String joined = Joiner.on("+").withKeyValueSeparator(":").join(query);

        URL url = HttpUrl.get(Endpoint.SEARCH.toURL())
                .newBuilder()
                .setEncodedQueryParameter("q", joined)
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", "100")
                .build()
                .url();

        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        TimeUnit.MILLISECONDS.sleep(500);
        return response.getRight().getAsJsonObject();
    }


    @SneakyThrows(InterruptedException.class)
    public JsonObject fetchRepoInfo(String name) {
        URL url = Endpoint.REPOSITORY.toURL(name.split("/"));
        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        TimeUnit.MILLISECONDS.sleep(500);
        return response.getRight().getAsJsonObject();
    }

    public GitCommit fetchLastCommitInfo(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_COMMITS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        JsonArray commits = response.getRight().getAsJsonArray();
        try {
            JsonObject latest = commits.get(0).getAsJsonObject();
            return conversionService.convert(latest, GitCommit.class);
        } catch (IndexOutOfBoundsException ignored) {
            /*
             * It might be possible for a repository to have no commits.
             * However, such repositories should never appear in the search,
             * because we target repositories written in a specific language!
             * Still, better safe than sorry...
            */
            return GitCommit.NULL_COMMIT;
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

    public Long fetchNumberOfLabels(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LABELS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    public Long fetchNumberOfLanguages(String name) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LANGUAGES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", "1")
                .addQueryParameter("per_page", "1")
                .build()
                .url();
        return fetchLastPageNumberFromHeader(url);
    }

    private Long fetchLastPageNumberFromHeader(URL url) {
        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        HttpStatus status = response.getLeft();

        Long count;
        if (status == HttpStatus.FORBIDDEN) {
            /*
             * Response status code 403, two possibilities:
             * (1) The rate limit for the current token is exceeded
             * (2) The request is too expensive for GitHub to compute
             * (eg. https://api.github.com/repos/torvalds/linux/contributors)
             *
             * Since we make use of guards for the former case,
             * then the latter is always the response cause.
             * As a result we return null value to denote the metric as unobtainable.
             */
            count = null;
        } else {
            JsonElement element = response.getRight();
            Headers headers = response.getMiddle();
            String link = headers.get("link");
            if (link != null) {
                Map<String, String> links = new HashMap<>();
                Matcher matcher = HEADER_LINK_PATTERN.matcher(link);
                while (matcher.find()) {
                    links.put(matcher.group(2), matcher.group(1));
                }
                HttpUrl last = HttpUrl.get(links.get("last"));
                count = Long.parseLong(last.queryParameter("page"));
            } else if (element.isJsonArray()) {
                count = (long) element.getAsJsonArray().size();
            } else if (element.isJsonObject()) {
                count = (long) element.getAsJsonObject().size();
            } else {
                count = 1L;
            }
        }
        return count;
    }

    @SneakyThrows(InterruptedException.class)
    public JsonArray fetchRepoLabels(String name, Integer page) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LABELS.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        TimeUnit.MILLISECONDS.sleep(500);
        return response.getRight().getAsJsonArray();
    }

    @SneakyThrows(InterruptedException.class)
    public JsonObject fetchRepoLanguages(String name, Integer page) {
        URL url = HttpUrl.get(Endpoint.REPOSITORY_LANGUAGES.toURL(name.split("/")))
                .newBuilder()
                .addQueryParameter("page", page.toString())
                .addQueryParameter("per_page", "100")
                .build()
                .url();
        Triple<HttpStatus, Headers, JsonElement> response = makeAPICall(url);
        TimeUnit.MILLISECONDS.sleep(500);
        return response.getRight().getAsJsonObject();
    }


    @SuppressWarnings({ "ConstantConditions", "resource" })
    @SneakyThrows({ IOException.class, InterruptedException.class })
    Triple<HttpStatus, Headers, JsonElement> makeAPICall(URL url) {
        int tryNum = 0;
        while (tryNum < RETRY_MAX_ATTEMPTS) {
            tryNum++;
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            String currentToken = gitHubTokenManager.getCurrentToken();
            if (currentToken != null)
                builder.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + currentToken);
            Request request = builder.build();

            Response response = client.newCall(request).execute();
            HttpStatus status = HttpStatus.valueOf(response.code());
            Headers headers = response.headers();
            String body = response.body().string();

            if (response.isSuccessful()) {
                return Triple.of(status, headers, JsonParser.parseString(body));
            } else if (status == HttpStatus.UNAUTHORIZED) {
                /*
                 * Here we should not call `replaceTokenIfExpired()`
                 * since it would lead to an infinite loop,
                 * because we are checking the Rate Limit API
                 * with the very same unauthorized token.
                */
                gitHubTokenManager.replaceToken();
                TimeUnit.SECONDS.sleep(5);
            } else if (status == HttpStatus.FORBIDDEN) {
                /*
                 * Response status code 403, two possibilities:
                 * (1) The rate limit for the current token is exceeded
                 * (2) The request is too expensive for GitHub to compute
                 * (eg. https://api.github.com/repos/torvalds/linux/contributors)
                */
                String xRateLimitRemaining = headers.get("X-RateLimit-Remaining");
                if (xRateLimitRemaining != null) {
                    int rateLimitRemaining = Integer.parseInt(xRateLimitRemaining);
                    if (rateLimitRemaining > 0) {
                        if(!body.contains("too large")) {
                            log.error(
                                    "\n**********************************************************************\n" +
                                    "- Status code is 403, but the rate limit is not exceeded!\n" +
                                    "- The returned response contains no mention of the result being 'too long'!\n" +
                                    " ################ AN UPDATE TO THE LOGIC IS REQUIRED ################ \n" +
                                    "**********************************************************************"
                            );
                        }
                        return Triple.of(status, headers, JsonParser.parseString(body));
                    }
                }
                log.info("Try #{}: Response Code = {}, X-RateLimit-Remaining = {}", tryNum, status, xRateLimitRemaining);
                gitHubTokenManager.replaceTokenIfExpired();
            } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                gitHubTokenManager.replaceTokenIfExpired();
            } else if (status.is4xxClientError()) {
                log.error("Try #{}: GitHub Client Error Encountered [{}]", tryNum, status);
                waitBeforeRetry();
            } else if (status.is5xxServerError()) {
                log.error("Try #{}: GitHub Server Error Encountered [{}]", tryNum, status);
                waitBeforeRetry();
            } else {
                log.error("Try #{}: Response Code = {}, Request URL = {}", tryNum, status, url);
                gitHubTokenManager.replaceTokenIfExpired();
            }
        }
        log.error("Failed after {} try. SKIPPING.", RETRY_MAX_ATTEMPTS);
        return null;
    }
}
