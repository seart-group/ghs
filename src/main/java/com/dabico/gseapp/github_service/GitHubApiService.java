package com.dabico.gseapp.github_service;

import com.dabico.gseapp.util.interval.DateInterval;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubApiService {
    static Logger logger = LoggerFactory.getLogger(GitHubApiService.class);

    OkHttpClient client;
    final static int MIN_STARS = 10;

    @Autowired
    public GitHubApiService(){
        this.client = new OkHttpClient.Builder()
                                      .connectTimeout(1, TimeUnit.MINUTES)
                                      .writeTimeout(1, TimeUnit.MINUTES)
                                      .readTimeout(1, TimeUnit.MINUTES)
                                      .build();
    }

    public Response searchRepositories(String language, DateInterval interval, Integer page, String token,
                                       Boolean crawl_updated_repos) throws IOException, InterruptedException
    {
        String language_encoded = URLEncoder.encode(language, StandardCharsets.UTF_8);
        String url = Endpoints.SEARCH_REPOS.getUrl() + "?q=language:" + language_encoded +
                (crawl_updated_repos ? "+pushed:" : "+created:") + interval +
                "+fork:true+stars:>="+MIN_STARS+"+is:public&page=" + page + "&per_page=100";

//      String url = Endpoints.SEARCH_REPOS.getUrl() + "?q=repo:XXX/YYYYY+fork:true";
//        String url = Endpoints.SEARCH_REPOS.getUrl() + "?q=repo:zenedge/zentables-addons+fork:true";

        logger.info("Github API Call: "+url);
        Response response = makeAPICall(url, token);

        //TODO Remove guards when done
        Thread.sleep(1000);
        return response;
    }

    public boolean isTokenLimitExceeded(String token) throws IOException {
        Response response = makeAPICall(Endpoints.LIMIT.getUrl(),token);
        ResponseBody responseBody = response.body();
        if (response.isSuccessful() && responseBody != null){
            JsonObject bodyJson = JsonParser.parseString(responseBody.string()).getAsJsonObject();
            response.close();
            JsonObject search = bodyJson.get("resources").getAsJsonObject().get("search").getAsJsonObject();
            int remaining = search.get("remaining").getAsInt();
            return remaining <= 0;
        }  else if (response.code() == 401) {
            logger.error("**************** Invalid Access Token [401 Unauthorized]: {} ****************", token);
            logger.error("**************** Exiting gse app due to invalid token  ****************", token);
            System.exit(401);
            return false;
        }
        else {
            throw new HttpResponseException(response.code(),"GitHub Server Error");
        }
    }

    public Response searchRepoLabels(String name, String token) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 labels used THEORETICALLY SHOULD NOT HAPPEN
        Response response = makeAPICall(generateLabelsURL(name) + "?page=1&per_page=100",token);
        Thread.sleep(2000);
        return response;
    }

    public Response searchRepoLanguages(String name, String token) throws IOException, InterruptedException {
        //TODO Adjust scalability for more than 100 languages used THEORETICALLY SHOULD NOT HAPPEN
        Response response = makeAPICall(generateLanguagesURL(name) + "?page=1&per_page=100",token);
        Thread.sleep(2000);
        return response;
    }

    private Response makeAPICall(String reqURL, String token) throws IOException {
        return client.newCall(generateRequest(reqURL,token)).execute();
    }

    private Request generateRequest(String reqURL, String token){
        return new Request.Builder()
                          .url(reqURL)
                          .addHeader("Authorization", "token " + token)
                          .addHeader("Accept", "application/vnd.github.v3+json")
                          .build();
    }

    private String generateRepoURL(String name){ return Endpoints.REPOS.getUrl() + "/" + name; }

    private String generateLabelsURL(String name){ return generateRepoURL(name) + "/labels"; }

    private String generateLanguagesURL(String name){ return generateRepoURL(name) + "/languages"; }
}
