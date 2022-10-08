package usi.si.seart.github;

import com.google.common.collect.Range;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import usi.si.seart.util.Dates;
import usi.si.seart.util.Ranges;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubApiService {

    private static final int minStars = 10;

    private static final long retrySleepPeriod = 60000L;
    private static final int maxRetryCount = 3;

    OkHttpClient client;
    DateFormat utcTimestampFormat;

    @NonFinal
    GitHubCredentialUtil gitHubCredentialUtil;

    public void setGitHubCredentialUtil(GitHubCredentialUtil gitHubCredentialUtil) {
        this.gitHubCredentialUtil = gitHubCredentialUtil;
    }

    public String searchRepositories(
            String language, Range<Date> dateRange, Integer page, boolean crawlUpdatedRepos
    ) throws IOException, InterruptedException {
        String languageEncoded = URLEncoder.encode(language, StandardCharsets.UTF_8);
        String url = Endpoints.SEARCH_REPOS.getUrl() + "?q=language:" + languageEncoded +
                (crawlUpdatedRepos ? "+pushed:" : "+created:") + Ranges.toString(dateRange, utcTimestampFormat) +
                "+fork:true+stars:>="+ minStars +"+is:public&page=" + page + "&per_page=100";

        // For debugging specific repositories
        // url = generateSearchRepo("XXXXX/YYYYY");
        // url = generateSearchRepo("torvalds/linux"); // only pulls
        // url = generateSearchRepo("davidwernhart/AlDente"); // both issue and pulls
        // url = generateSearchRepo("seart-group/ghs"); // only issues

        log.info("Github API Call: {}", url);
        Triple<Integer, Headers, String> response = makeAPICall(url);
        String bodyStr = response.getRight();

        Thread.sleep(1000);
        return bodyStr;
    }


    public String fetchRepoInfo(String repoFullName) throws IOException, InterruptedException {
        Triple<Integer, Headers, String> response = makeAPICall(generateRepoURL(repoFullName));
        String bodyStr = response.getRight();
        Thread.sleep(500);
        return bodyStr;
    }


    public Long fetchNumberOfCommits(String repoFullName) throws IOException, InterruptedException {
        // https://api.github.com/repos/07th-mod/higurashi-console-arcs/commits?per_page=1
        return fetchLastPageNumberFromHeader(generateCommitsURL(repoFullName) + "?page=1&per_page=1");
    }

    public Pair<String, Date> fetchLastCommitInfo(String repoFullName) throws IOException, InterruptedException {
        // https://api.github.com/repos/07th-mod/higurashi-console-arcs/commits?per_page=1
        Triple<Integer, Headers, String> response = makeAPICall(generateCommitsURL(repoFullName) + "?page=1&per_page=1");
        String bodyStr = response.getRight();
        JsonObject latestCommitJson = JsonParser.parseString(bodyStr).getAsJsonArray().get(0).getAsJsonObject();
        String sha = latestCommitJson.get("sha").getAsString();
        String dateStr = latestCommitJson.get("commit").getAsJsonObject().get("committer").getAsJsonObject().get("date").getAsString();
        Date date = Dates.fromGitDateString(dateStr);
        return Pair.of(sha,date);
    }

    public Long fetchNumberOfBranches(String repoFullName) throws IOException, InterruptedException {
        //Branches: https://api.github.com/repos/07th-mod/higurashi-console-arcs/branches?per_page=1
        return fetchLastPageNumberFromHeader(generateBranchesURL(repoFullName) + "?page=1&per_page=1");
    }

    public Long fetchNumberOfReleases(String repoFullName) throws IOException, InterruptedException {
        //Releases: https://api.github.com/repos/07th-mod/higurashi-console-arcs/releases?per_page=1
        return fetchLastPageNumberFromHeader(generateReleasesURL(repoFullName) + "?page=1&per_page=1");
    }


    public Long fetchNumberOfContributors(String repoFullName) throws IOException, InterruptedException {
        //Releases: https://api.github.com/repos/07th-mod/higurashi-console-arcs/contributors?per_page=1
        return fetchLastPageNumberFromHeader(generateContributorsURL(repoFullName) + "?page=1&per_page=1");
    }

    public Long fetchNumberOfOpenIssuesAndPulls(String repoFullName) throws IOException, InterruptedException {
        //Issues+Pull Open: https://api.github.com/repos/07th-mod/higurashi-console-arcs/issues?state=open&per_page=1
        return fetchLastPageNumberFromHeader(generateIssuesURL(repoFullName) + "?state=open&page=1&per_page=1");
    }
    public Long fetchNumberOfAllIssuesAndPulls(String repoFullName) throws IOException, InterruptedException {
        //Issues+Pull All: https://api.github.com/repos/07th-mod/higurashi-console-arcs/issues?state=all&per_page=1
        return fetchLastPageNumberFromHeader(generateIssuesURL(repoFullName) + "?state=all&page=1&per_page=1");
    }

    public Long fetchNumberOfOpenPulls(String repoFullName) throws IOException, InterruptedException {
        //Pull Open: https://api.github.com/repos/07th-mod/higurashi-console-arcs/pulls?state=open&per_page=1
        return fetchLastPageNumberFromHeader(generatePullsURL(repoFullName) + "?state=open&page=1&per_page=1");
    }
    public Long fetchNumberOfAllPulls(String repoFullName) throws IOException, InterruptedException {
        //Pull All: https://api.github.com/repos/07th-mod/higurashi-console-arcs/pulls?state=all&per_page=1
        return fetchLastPageNumberFromHeader(generatePullsURL(repoFullName) + "?state=all&page=1&per_page=1");
    }

    public Long fetchNumberOfLabels(String repoFullName) throws IOException, InterruptedException {
        return fetchLastPageNumberFromHeader(generateLabelsURL(repoFullName) + "?page=1&per_page=1");
    }

    public Long fetchNumberOfLanguages(String repoFullName) throws IOException, InterruptedException {
        return fetchLastPageNumberFromHeader(generateLanguagesURL(repoFullName) + "?page=1&per_page=1");
    }

    private Long fetchLastPageNumberFromHeader(String url) throws IOException, InterruptedException {
        Triple<Integer, Headers, String> response = makeAPICall(url);
        Integer retCode = response.getLeft();

        long lastPageCount;
        if(retCode == HttpStatus.FORBIDDEN.value())
            // Forbidden 403 - two possibilities: (1) Token limit is exceeded, (2) too expensive computation as for https://api.github.com/repos/torvalds/linux/contributors
            // but makeAPICall doesn't return value if token limit is exceeded, so it's the latter case.
            lastPageCount = Long.MAX_VALUE;
        else {
            Headers headers = response.getMiddle();
            String linkField = headers.get("link");
            if (linkField != null) {
                String lastPageLink = linkField.split(",")[1];
                String lastPageStr = lastPageLink.substring(lastPageLink.indexOf("page=") + ("page=".length()), lastPageLink.indexOf("&", lastPageLink.indexOf("page=")));
                lastPageCount = Long.parseLong(lastPageStr);
            } else if(response.getRight().equals("[]")) {
                lastPageCount = 0L;
            } else {
                lastPageCount = 1L;
            }
        }
        return lastPageCount;
    }

    public String fetchRepoLabels(String repoFullName, int page) throws IOException, InterruptedException {
        String url = String.format("%s?page=%d&per_page=100", generateLabelsURL(repoFullName), page);
        Triple<Integer, Headers, String> response = makeAPICall(url);
        String responseStr = response.getRight();
        Thread.sleep(1000);
        return responseStr;
    }

    public String fetchRepoLanguages(String repoFullName, int page) throws IOException, InterruptedException {
        String url = String.format("%s?page=%d&per_page=100", generateLanguagesURL(repoFullName), page);
        Triple<Integer, Headers, String> response = makeAPICall(url);
        String responseStr = response.getRight();
        Thread.sleep(1000);
        return responseStr;
    }


    Triple<Integer, Headers, String> makeAPICall(String reqURL) throws IOException, InterruptedException {
        int tryNum = 0;
        while (tryNum < maxRetryCount) {
            tryNum++;
            Response response = client.newCall(generateRequest(reqURL, gitHubCredentialUtil.getCurrentToken())).execute();
            Headers headers = response.headers();
            ResponseBody body = response.body();
            String bodyStr = null;
            if (body != null) {
                bodyStr = body.string();
            } else {
                log.error("**********************************************************************");
                log.error("How come 'body' object is null? reqURL = {}\", reqURL", reqURL);
                log.error("**********************************************************************");
            }
            response.close();

            if (response.isSuccessful() && body != null) {
                return Triple.of(response.code(), headers, bodyStr);
            } else if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                log.error("**************** Invalid Access Token [401 Unauthorized]: {} ****************", gitHubCredentialUtil.getCurrentToken());
                // Here we should not call `replaceTokenIfExpired()`, otherwise it leads to an infinite loop,
                // because that method calls Rate API with the very same unauthorized token.
                gitHubCredentialUtil.getNewToken();
                Thread.sleep(5000);
            } else if (response.code() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                gitHubCredentialUtil.replaceTokenIfExpired();
            } else if (response.code() == HttpStatus.FORBIDDEN.value()) {
                // Forbidden 403 - two possibilities: (1) Token limit is exceeded, (2) too expensive computation as for https://api.github.com/repos/torvalds/linux/contributors
                String rateLimitRemainingStr = headers.get("X-RateLimit-Remaining");
                if (rateLimitRemainingStr != null) {
                    int rateLimitRemaining = Integer.parseInt(rateLimitRemainingStr);
                    if (rateLimitRemaining > 0) {
                        if(bodyStr.contains("too large")==false) {
                            log.error("**********************************************************************");
                            log.error("403 but limit not exceeded. So we expected 'too long' in message. but not found!");
                            log.error("Update the logic. reqURL = {}", reqURL);
                            log.error("**********************************************************************");
                        }
                        return Triple.of(response.code(), headers, bodyStr);
                    }
                }
                log.info("Try #{}: 403 Error. response code = {} - X-RateLimit-Remaining={}", tryNum, response.code(), rateLimitRemainingStr);
                gitHubCredentialUtil.replaceTokenIfExpired();
            } else if (response.code() >= 500) {
                log.error("Try #{}: GitHub Server Error Encountered: {}", tryNum, response.code());
                Thread.sleep(retrySleepPeriod);
                log.error("Retrying...");
            } else {
                log.error("Try #{}: Failed to execute API call. retCode={} isSuccess={} - reqURL={}", tryNum, response.code(), response.isSuccessful(), reqURL);
                gitHubCredentialUtil.replaceTokenIfExpired();
            }
        }
        log.error("Failed after {} try. SKIPPING.", maxRetryCount);
        return null;
    }

    private Request generateRequest(String reqURL, String token){
        return new Request.Builder()
                          .url(reqURL)
                          .addHeader("Authorization", "token " + token)
                          .addHeader("Accept", "application/vnd.github.v3+json")
                          .build();
    }

    private String generateRepoURL(String repoFullName){
        return Endpoints.REPOS.getUrl() + "/" + repoFullName;
    }

    private String generateSearchRepo(String repoFullName) {
        return Endpoints.SEARCH_REPOS.getUrl() + "?q=fork:true+repo:" + repoFullName;
    }

    private String generateCommitsURL(String repoFullName){ return generateRepoURL(repoFullName) + "/commits"; }

    private String generateBranchesURL(String repoFullName){ return generateRepoURL(repoFullName) + "/branches"; }

    private String generateReleasesURL(String repoFullName){ return generateRepoURL(repoFullName) + "/releases"; }

    private String generateLabelsURL(String repoFullName){ return generateRepoURL(repoFullName) + "/labels"; }

    private String generateLanguagesURL(String repoFullName){ return generateRepoURL(repoFullName) + "/languages"; }

    private String generateContributorsURL(String repoFullName){ return generateRepoURL(repoFullName) + "/contributors"; }

    private String generateIssuesURL(String repoFullName){ return generateRepoURL(repoFullName) + "/issues"; }

    private String generatePullsURL(String repoFullName){ return generateRepoURL(repoFullName) + "/pulls"; }
}
