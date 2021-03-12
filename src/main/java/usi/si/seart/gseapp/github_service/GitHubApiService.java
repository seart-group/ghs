package usi.si.seart.gseapp.github_service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.NonFinal;
import okhttp3.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import usi.si.seart.gseapp.util.DateUtils;
import usi.si.seart.gseapp.util.interval.DateInterval;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubApiService {
    static Logger logger = LoggerFactory.getLogger(GitHubApiService.class);

    OkHttpClient client;
    final static int MIN_STARS = 10;

    final static long retrySleepPeriod_ms = 60000L;
    final static int maxRetryCount = 3;

    final static int STATUS_UNAUTHORIZED = 401;
    final static int STATUS_FORBIDDEN = 403;
    final static int STATUS_TOO_MANY_REQUESTS = 429;

    @NonFinal
    GitHubCredentialUtil gitHubCredentialUtil;


    @Autowired
    public GitHubApiService()
    {
        this.client = new OkHttpClient.Builder()
                                      .connectTimeout(1, TimeUnit.MINUTES)
                                      .writeTimeout(1, TimeUnit.MINUTES)
                                      .readTimeout(1, TimeUnit.MINUTES)
                                      .build();

    }

    public void setGitHubCredentialUtil(GitHubCredentialUtil gitHubCredentialUtil) {
        this.gitHubCredentialUtil = gitHubCredentialUtil;
    }



    public String searchRepositories(String language, DateInterval interval, Integer page,
                                         Boolean crawl_updated_repos) throws IOException, InterruptedException
    {
        String language_encoded = URLEncoder.encode(language, StandardCharsets.UTF_8);
        String url = Endpoints.SEARCH_REPOS.getUrl() + "?q=language:" + language_encoded +
                (crawl_updated_repos ? "+pushed:" : "+created:") + interval +
                "+fork:true+stars:>="+MIN_STARS+"+is:public&page=" + page + "&per_page=100";

        // For debugging specific repository
//        url = generateSearchRepo("XXXXX/YYYYY");
//        url = generateSearchRepo("torvalds/linux"); // only pulls
//        url = generateSearchRepo("davidwernhart/AlDente"); // both issue and pulls
//        url = generateSearchRepo("seart-group/ghs"); // only issues


        logger.info("Github API Call: "+url);
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
        Long n = fetchLastPageNumberFromHeader(generateCommitsURL(repoFullName) + "?page=1&per_page=1");
        return n;
    }

    public Pair<String, Date> fetchLastCommitInfo(String repoFullName) throws IOException, InterruptedException {
        // https://api.github.com/repos/07th-mod/higurashi-console-arcs/commits?per_page=1
        Triple<Integer, Headers, String> response = makeAPICall(generateCommitsURL(repoFullName) + "?page=1&per_page=1");
        String bodyStr = response.getRight();
        JsonObject latestCommitJson = JsonParser.parseString(bodyStr).getAsJsonArray().get(0).getAsJsonObject();
        String sha = latestCommitJson.get("sha").getAsString();
        String dateStr = latestCommitJson.get("commit").getAsJsonObject().get("committer").getAsJsonObject().get("date").getAsString();
        Date date = DateUtils.fromGitDateString(dateStr);
        return Pair.of(sha,date);
    }

    public Long fetchNumberOfBranches(String repoFullName) throws IOException, InterruptedException {
        //Branches: https://api.github.com/repos/07th-mod/higurashi-console-arcs/branches?per_page=1
        Long n = fetchLastPageNumberFromHeader(generateBranchesURL(repoFullName) + "?page=1&per_page=1");
        return n;
    }

    public Long fetchNumberOfReleases(String repoFullName) throws IOException, InterruptedException {
        //Releases: https://api.github.com/repos/07th-mod/higurashi-console-arcs/releases?per_page=1
        Long n = fetchLastPageNumberFromHeader(generateReleasesURL(repoFullName) + "?page=1&per_page=1");
        return n;
    }


    public Long fetchNumberOfContributors(String repoFullName) throws IOException, InterruptedException {
        //Releases: https://api.github.com/repos/07th-mod/higurashi-console-arcs/contributors?per_page=1
        Long n = fetchLastPageNumberFromHeader(generateContributorsURL(repoFullName) + "?page=1&per_page=1");
        return n;
    }

    public Long fetchNumberOfOpenIssuesAndPulls(String repoFullName) throws IOException, InterruptedException {
        //Issues+Pull Open: https://api.github.com/repos/07th-mod/higurashi-console-arcs/issues?state=open&per_page=1
        Long n = fetchLastPageNumberFromHeader(generateIssuesURL(repoFullName) + "?state=open&page=1&per_page=1");
        return n;
    }
    public Long fetchNumberOfAllIssuesAndPulls(String repoFullName) throws IOException, InterruptedException {
        //Issues+Pull All: https://api.github.com/repos/07th-mod/higurashi-console-arcs/issues?state=all&per_page=1
        Long n = fetchLastPageNumberFromHeader(generateIssuesURL(repoFullName) + "?state=all&page=1&per_page=1");
        return n;
    }

    public Long fetchNumberOfOpenPulls(String repoFullName) throws IOException, InterruptedException {
        //Pull Open: https://api.github.com/repos/07th-mod/higurashi-console-arcs/pulls?state=open&per_page=1
        Long n = fetchLastPageNumberFromHeader(generatePullsURL(repoFullName) + "?state=open&page=1&per_page=1");
        return n;
    }
    public Long fetchNumberOfAllPulls(String repoFullName) throws IOException, InterruptedException {
        //Pull All: https://api.github.com/repos/07th-mod/higurashi-console-arcs/pulls?state=all&per_page=1
        Long n = fetchLastPageNumberFromHeader(generatePullsURL(repoFullName) + "?state=all&page=1&per_page=1");
        return n;
    }

    private Long fetchLastPageNumberFromHeader(String url) throws IOException, InterruptedException {
        Triple<Integer, Headers, String> response = makeAPICall(url);
        Integer retCode = response.getLeft();

        Long lastPageCount;
        if(retCode == STATUS_FORBIDDEN)
            // Forbidden 403 - two possibilities: (1) Token limit is exceeded, (2) too expensive computation as for https://api.github.com/repos/torvalds/linux/contributors
            // but makeAPICall doesn't return value if token limit is exceeded, so it's the latter case.
            lastPageCount = Long.MAX_VALUE;
        else
        {
            Headers headers = response.getMiddle();
            String link_field = headers.get("link");
            if(link_field != null) {
                String link_lastPage = link_field.split(",")[1];
                String lastPageStr = link_lastPage.substring(link_lastPage.indexOf("page=") + ("page=".length()), link_lastPage.indexOf("&", link_lastPage.indexOf("page=")));
                lastPageCount = Long.parseLong(lastPageStr);
            }
            else if(response.getRight().equals("[]"))
                lastPageCount = 0L;
            else
                lastPageCount = 1L;
        }
        return lastPageCount;
    }

    public String fetchRepoLabels(String repoFullName) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 labels used THEORETICALLY SHOULD NOT HAPPEN
        Triple<Integer, Headers, String> response = makeAPICall(generateLabelsURL(repoFullName) + "?page=1&per_page=100");
        String responseStr = response.getRight();
        Thread.sleep(1000);
        return responseStr;
    }

    public String fetchRepoLanguages(String repoFullName) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 languages used THEORETICALLY SHOULD NOT HAPPEN
        Triple<Integer, Headers, String> response = makeAPICall(generateLanguagesURL(repoFullName) + "?page=1&per_page=100");
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
            if (body != null)
                bodyStr = body.string();
            else
            {
                logger.error("**********************************************************************");
                logger.error("How come 'body' object is null? reqURL = {}\", reqURL", reqURL);
                logger.error("**********************************************************************");
            }
            response.close();

            if (response.isSuccessful() && body != null) {
                return Triple.of(response.code(), headers, bodyStr);
            } else if (response.code() == STATUS_UNAUTHORIZED) {
                logger.error("**************** Invalid Access Token [401 Unauthorized]: {} ****************", gitHubCredentialUtil.getCurrentToken());
                // Here we should not call `replaceTokenIfExpired()`, otherwise it leads to an infinite loop,
                // because that method calls Rate API with the very same unauthorized token.
                gitHubCredentialUtil.GetANewToken();
                Thread.sleep(5000);
            } else if (response.code() == STATUS_TOO_MANY_REQUESTS) {
                gitHubCredentialUtil.replaceTokenIfExpired();
            } else if (response.code() == STATUS_FORBIDDEN) {
                // Forbidden 403 - two possibilities: (1) Token limit is exceeded, (2) too expensive computation as for https://api.github.com/repos/torvalds/linux/contributors
                String rateLimitRemainingStr = headers.get("X-RateLimit-Remaining");
                if (rateLimitRemainingStr != null) {
                    int rateLimitRemaining = Integer.parseInt(rateLimitRemainingStr);
                    if (rateLimitRemaining > 0) {
                        if(bodyStr.contains("too large")==false) {
                            logger.error("**********************************************************************");
                            logger.error("403 but limit not exceeded. So we expected 'too long' in message. but not found!");
                            logger.error("Update the logic. reqURL = {}", reqURL);
                            logger.error("**********************************************************************");
                        }
                        return Triple.of(response.code(), headers, bodyStr);
                    }
                }
                logger.info("Try #{}: 403 Error. response code = {} - X-RateLimit-Remaining={}", tryNum, response.code(), rateLimitRemainingStr);
                gitHubCredentialUtil.replaceTokenIfExpired();
            } else if (response.code() >= 500) {
                logger.error("Try #{}: GitHub Server Error Encountered: {}", tryNum, response.code());
                Thread.sleep(retrySleepPeriod_ms);
                logger.error("Retrying...");
            } else {
                logger.error("Try #{}: Failed to execute API call. retCode={} isSuccess={} - reqURL={}", tryNum, response.code(), response.isSuccessful(), reqURL);
                gitHubCredentialUtil.replaceTokenIfExpired();
            }
        }
        logger.error("Failed after {} try. SKIPPING.", maxRetryCount);
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
