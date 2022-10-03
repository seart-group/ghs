package usi.si.seart.gseapp.config;

import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usi.si.seart.gseapp.converter.AccessTokenToDtoConverter;
import usi.si.seart.gseapp.converter.CrawlJobToDtoConverter;
import usi.si.seart.gseapp.converter.GitRepoToDtoConverter;
import usi.si.seart.gseapp.converter.SupportedLanguageToDtoConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

@Configuration
public class MainConfig {

    @Bean
    public DateFormat utcTimestampFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    }

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull final CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET")
                        .allowedOrigins("http://localhost:3030")
                        .exposedHeaders("Links", "Download", "Content-Type", "Transfer-Encoding", "Date");
            }

            @Override
            public void addFormatters(@NotNull final FormatterRegistry registry) {
                 registry.addConverter(new AccessTokenToDtoConverter());
                 registry.addConverter(new SupportedLanguageToDtoConverter());
                 registry.addConverter(new CrawlJobToDtoConverter());
                 registry.addConverter(new GitRepoToDtoConverter());
            }
        };
    }

    @Bean
    FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        return bean;
    }
}
