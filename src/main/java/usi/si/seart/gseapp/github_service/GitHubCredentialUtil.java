package usi.si.seart.gseapp.github_service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.repository.AccessTokenRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubCredentialUtil {

    GitHubApiService gitHubApiService;
    AccessTokenRepository accessTokenRepository;

    @NonFinal
    int currentTokenIndex;

    @Getter
    @NonFinal
    String currentToken;

    List<String> accessTokens = new ArrayList<>();

    @Autowired
    public GitHubCredentialUtil(GitHubApiService gitHubApiService,
                                AccessTokenRepository accessTokenRepository)
    {
        this.accessTokenRepository = accessTokenRepository;
        this.gitHubApiService = gitHubApiService;
        this.gitHubApiService.setGitHubCredentialUtil(this);

        getTokensFromDB();
        currentTokenIndex = 0;
        currentToken = accessTokens.get(currentTokenIndex);
    }

    void getNewToken(){
        currentTokenIndex = (currentTokenIndex + 1) % accessTokens.size();
        currentToken = accessTokens.get(currentTokenIndex);
    }

    private void getTokensFromDB(){
        accessTokens.clear();
        accessTokenRepository.findAll().forEach(accessToken -> accessTokens.add(accessToken.getValue()));
        if (accessTokens.isEmpty()) {
            log.error("**************** No Access Token Found ****************");
            System.exit(1);
        }
    }

    public void replaceTokenIfExpired() throws IOException, InterruptedException {
        Triple<Integer, Headers, String> response = gitHubApiService.makeAPICall(Endpoints.LIMIT.getUrl());
        String bodyStr = response.getRight();

        if (bodyStr != null) {
            JsonObject result = JsonParser.parseString(bodyStr).getAsJsonObject();
            JsonObject resources = result.get("resources").getAsJsonObject();

            int remainingCore = resources.get("core").getAsJsonObject().get("remaining").getAsInt();
            int remainingSearch = resources.get("search").getAsJsonObject().get("remaining").getAsInt();
            boolean isTokenLimitExceeded = (remainingCore <= 0 || remainingSearch <= 0);
            if (isTokenLimitExceeded) {
                getNewToken();
                long l = calculateWaitingTime(result);
                if (l > 0) {
                    try {
                        log.info("[[Sleeping {} Sec]]", l);
                        TimeUnit.SECONDS.sleep(l + 1);
                    } catch (InterruptedException e) {
                        log.error("I was interrupted while I was waiting for GitHub cool-down.", e);
                    }
                }
            }
        } else {
            log.error("Failed to use GitHub Limit API");
        }
    }

    public long calculateWaitingTime(JsonObject rateLimitJson) {
        long searchWaitSec;
        long coreWaitSec;

        long nowEpochSecond = Instant.now().getEpochSecond();
        JsonObject resourcesObj = rateLimitJson.getAsJsonObject("resources");
        ///////////// Core
        JsonObject coreObj = resourcesObj.getAsJsonObject("core");
        int coreLimit = coreObj.get("limit").getAsInt();
        int coreRemaining = coreObj.get("remaining").getAsInt();
        long coreResetEpochSecond = coreObj.get("reset").getAsLong();
        if (coreRemaining > 0) {
            coreWaitSec = 0;
        } else {
            coreWaitSec = coreResetEpochSecond - nowEpochSecond;
        }
        ///////////// Search
        JsonObject searchObj = resourcesObj.getAsJsonObject("search");
        int searchLimit = searchObj.get("limit").getAsInt();
        int searchRemaining = searchObj.get("remaining").getAsInt();
        long searchResetEpochSecond = searchObj.get("reset").getAsLong();
        if (searchRemaining > 0) {
            searchWaitSec = 0;
        } else {
            searchWaitSec = searchResetEpochSecond - nowEpochSecond;
        }
        log.info("Search Limit: {}/min  Remaining: {}  -- Reset: {}  Now: {} => Search Wait {} Sec | Core Limit: {}/min  Remaining: {}  -- Reset: {}  Now: {} => Core Wait {} Sec ",
                searchLimit, searchRemaining, searchResetEpochSecond, nowEpochSecond, searchWaitSec,
                coreLimit, coreRemaining, coreResetEpochSecond, nowEpochSecond, coreWaitSec);

        return Math.max(coreWaitSec, searchWaitSec);
    }

}
