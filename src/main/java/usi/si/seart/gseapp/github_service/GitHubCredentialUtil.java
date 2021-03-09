package usi.si.seart.gseapp.github_service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import okhttp3.Headers;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import usi.si.seart.gseapp.repository.AccessTokenRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubCredentialUtil {
    static Logger logger = LoggerFactory.getLogger(GitHubCredentialUtil.class);

    GitHubApiService gitHubApiService;
    AccessTokenRepository accessTokenRepository;


    @NonFinal
    int tokenOrdinal;
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


        getAccessTokens();
        this.tokenOrdinal = -1;
        this.currentToken = getNewToken();
    }

    public String getCurrentToken()
    {
        return currentToken;
    }

    private void getAccessTokens(){
        accessTokens.clear();
        accessTokenRepository.findAll().forEach(accessToken -> accessTokens.add(accessToken.getValue()));
        if(accessTokens.size()==0)
        {
            logger.error("**************** No Access Token Found ****************");
            logger.error("**************** Exiting gse app due to lack of access token  ****************");
            System.exit(1);
        }
    }

    public void replaceTokenIfExpired() throws IOException, InterruptedException {

        Triple<Integer, Headers, String> response = gitHubApiService.makeAPICall(Endpoints.LIMIT.getUrl());
        String bodyStr = response.getRight();

        if(bodyStr!=null)
        {
            JsonObject result = JsonParser.parseString(bodyStr).getAsJsonObject();

            int remaining_core = result.get("resources").getAsJsonObject().get("core").getAsJsonObject().get("remaining").getAsInt();
            int remaining_search = result.get("resources").getAsJsonObject().get("search").getAsJsonObject().get("remaining").getAsInt();

            logger.debug("******** TOKEN: {} -- CORE: {} -- SEARCH: {} **********", currentToken, remaining_core, remaining_search);

            boolean isTokenLimitExceeded = (remaining_core <= 0 || remaining_search<=0);
            if(isTokenLimitExceeded)
            {
                currentToken = getNewToken();
                long l = calculateWaitingTime(result);
                if(l>0)
                {
                    try
                    {
                        logger.info("[[Sleeping {}+2 Sec]]", l);
                        TimeUnit.SECONDS.sleep(l+2);
                    } catch (InterruptedException e)
                    {
                        logger.error("I was interrupted while I was waiting for GitHub cool-down.");
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            logger.error("Failed to use GitHub Limit API");
        }
    }

    private String getNewToken(){
        tokenOrdinal = (tokenOrdinal + 1) % accessTokens.size();
        return accessTokens.get(tokenOrdinal);
    }


    public long calculateWaitingTime(JsonObject rateLimitResponseJson)
    {
        long search_wait_sec=-1, core_wait_sec = -1;

        long now_epochSecond = Instant.now().getEpochSecond();
        JsonObject resourcesObj = rateLimitResponseJson.getAsJsonObject("resources");
        ///////////// Core
        JsonObject coreObj = resourcesObj.getAsJsonObject("core");
        int core_limit = coreObj.get("limit").getAsInt();
        int core_remaining = coreObj.get("remaining").getAsInt();
        long core_reset_epochSecond = coreObj.get("reset").getAsLong();
        if(core_remaining>0)
            core_wait_sec = 0;
        else
            core_wait_sec = core_reset_epochSecond-now_epochSecond;
        ///////////// Search
        JsonObject searchObj = resourcesObj.getAsJsonObject("search");
        int search_limit = searchObj.get("limit").getAsInt();
        int search_remaining = searchObj.get("remaining").getAsInt();
        long search_reset_epochSecond = searchObj.get("reset").getAsLong();
        if(search_remaining>0)
            search_wait_sec = 0;
        else
            search_wait_sec = search_reset_epochSecond-now_epochSecond;
        logger.info("Search Limit: {}/min  Remaining: {}  -- Reset: {}  Now: {} => Search Wait {} Sec | Core Limit: {}/min  Remaining: {}  -- Reset: {}  Now: {} => Core Wait {} Sec ",
                search_limit, search_remaining, search_reset_epochSecond, now_epochSecond, search_wait_sec,
                core_limit, core_remaining, core_reset_epochSecond, now_epochSecond, core_wait_sec);

        return Math.max(core_wait_sec, search_wait_sec);
    }

}
