package usi.si.seart.config;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import usi.si.seart.http.HeaderAttachmentInterceptor;
import usi.si.seart.http.LoggingInterceptor;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Bean
    public Headers headers() {
        return Headers.of(
                HttpHeaders.ACCEPT, "application/vnd.github+json",
                "X-GitHub-Api-Version", "2022-11-28"
        );
    }

    @Bean
    public LoggingInterceptor httpLoggingInterceptor() {
        return new LoggingInterceptor(LoggerFactory.getLogger(OkHttpClient.class));
    }

    @Bean
    public HeaderAttachmentInterceptor headerAttachmentInterceptor() {
        return new HeaderAttachmentInterceptor(headers());
    }

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(headerAttachmentInterceptor())
                .addNetworkInterceptor(httpLoggingInterceptor())
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
    }
}
