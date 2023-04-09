package usi.si.seart.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usi.si.seart.converter.GitRepoToDtoConverter;
import usi.si.seart.converter.JsonObjectToErrorResponseConverter;
import usi.si.seart.converter.JsonObjectToGitCommitConverter;
import usi.si.seart.converter.JsonObjectToGitRepoConverter;
import usi.si.seart.converter.JsonObjectToRateLimitConverter;
import usi.si.seart.converter.SupportedLanguageToDtoConverter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Configuration
public class MainConfig {

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneOffset.UTC);
    }

    @Bean
    public Function<Date, String> dateStringMapper() {
        return date -> {
            Instant instant = date.toInstant();
            Instant truncated = instant.truncatedTo(ChronoUnit.SECONDS);
            return dateTimeFormatter().format(truncated);
        };
    }

    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull final CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET")
                        .allowedOrigins(
                                "http://localhost:3030",
                                "http://localhost:7030",
                                "https://seart-ghs.si.usi.ch"
                        )
                        .exposedHeaders("Links", "Download", "Content-Type", "Transfer-Encoding", "Date");
            }

            @Override
            public void addFormatters(@NotNull final FormatterRegistry registry) {
                 registry.addConverter(new SupportedLanguageToDtoConverter());
                 registry.addConverter(new GitRepoToDtoConverter());
                 registry.addConverter(new JsonObjectToGitRepoConverter());
                 registry.addConverter(new JsonObjectToGitCommitConverter());
                 registry.addConverter(new JsonObjectToRateLimitConverter());
                 registry.addConverter(new JsonObjectToErrorResponseConverter());
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
