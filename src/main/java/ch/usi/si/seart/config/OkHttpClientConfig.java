package ch.usi.si.seart.config;

import ch.usi.si.seart.config.properties.GitHubProperties;
import ch.usi.si.seart.github.GitHubHttpHeaders;
import ch.usi.si.seart.github.GitHubMediaTypes;
import ch.usi.si.seart.github.GitHubRestConnector;
import ch.usi.si.seart.http.interceptor.HeaderAttachmentInterceptor;
import ch.usi.si.seart.http.interceptor.LoggingInterceptor;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpClientConfig {

    @Bean
    Headers headers(GitHubProperties properties) {
        return Headers.of(
                HttpHeaders.ACCEPT, "application/vnd.github+json",
                GitHubHttpHeaders.X_GITHUB_API_VERSION, properties.getApiVersion()
        );
    }

    @Bean
    LoggingInterceptor loggingInterceptor() {
        Logger logger = LoggerFactory.getLogger(GitHubRestConnector.class);
        return new LoggingInterceptor(logger);
    }

    @Bean
    HeaderAttachmentInterceptor headerAttachmentInterceptor(Headers headers) {
        return new HeaderAttachmentInterceptor(headers);
    }

    @Bean
    public OkHttpClient okHttpClient(
            HeaderAttachmentInterceptor headerAttachmentInterceptor, LoggingInterceptor loggingInterceptor
    ) {
        return new OkHttpClient.Builder()
                .addInterceptor(headerAttachmentInterceptor)
                .addNetworkInterceptor(loggingInterceptor)
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
    }
}
