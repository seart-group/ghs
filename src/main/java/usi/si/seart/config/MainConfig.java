package usi.si.seart.config;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usi.si.seart.converter.GitRepoDtoToCsvConverter;
import usi.si.seart.converter.GitRepoToDtoConverter;
import usi.si.seart.converter.JsonObjectToErrorResponseConverter;
import usi.si.seart.converter.JsonObjectToGitCommitConverter;
import usi.si.seart.converter.JsonObjectToGitRepoConverter;
import usi.si.seart.converter.JsonObjectToRateLimitConverter;
import usi.si.seart.converter.StringToContactsConverter;
import usi.si.seart.converter.StringToLicensesConverter;
import usi.si.seart.converter.SupportedLanguageToDtoConverter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Configuration
public class MainConfig {

    CsvMapper csvMapper;

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
    public Gson gson() {
        return new Gson();
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
                        .exposedHeaders(
                                "X-Link-Search",
                                "X-Link-Download"
                        );
            }

            @Override
            public void addFormatters(@NotNull final FormatterRegistry registry) {
                 registry.addConverter(new SupportedLanguageToDtoConverter());
                 registry.addConverter(new GitRepoToDtoConverter());
                 registry.addConverter(new JsonObjectToGitRepoConverter());
                 registry.addConverter(new GitRepoDtoToCsvConverter(csvMapper));
                 registry.addConverter(new JsonObjectToGitCommitConverter());
                 registry.addConverter(new JsonObjectToRateLimitConverter());
                 registry.addConverter(new JsonObjectToErrorResponseConverter());
                 registry.addConverter(new StringToContactsConverter());
                 registry.addConverter(new StringToLicensesConverter());
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
