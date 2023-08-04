package usi.si.seart.config;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usi.si.seart.collection.Ranges;
import usi.si.seart.converter.GitRepoDtoToCsvConverter;
import usi.si.seart.converter.GitRepoToDtoConverter;
import usi.si.seart.converter.JsonObjectToErrorResponseConverter;
import usi.si.seart.converter.JsonObjectToGitCommitConverter;
import usi.si.seart.converter.JsonObjectToGitRepoMetricConverter;
import usi.si.seart.converter.JsonObjectToRateLimitConverter;
import usi.si.seart.converter.SearchParameterDtoToGitRepoSearchConverter;
import usi.si.seart.converter.StringToContactsConverter;
import usi.si.seart.converter.StringToGitExceptionConverter;
import usi.si.seart.converter.StringToJsonElementConverter;
import usi.si.seart.converter.StringToJsonObjectConverter;
import usi.si.seart.converter.StringToLicensesConverter;
import usi.si.seart.converter.StringToNavigationLinksConverter;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Configuration
public class MainConfig {

    CsvMapper csvMapper;

    @Bean
    public Path tmpDir(@Value("${java.io.tmpdir}") String value) {
        return Path.of(value);
    }

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneOffset.UTC);
    }

    @Bean
    public Ranges.Splitter<Date> dateRangeSplitter() {
        return new Ranges.Splitter<>((lower, upper) -> {
            ZoneId zoneId = ZoneId.of("UTC");
            Instant lowerInstant = lower.toInstant();
            Instant upperInstant = upper.toInstant();
            ZonedDateTime lowerZoned = lowerInstant.atZone(zoneId);
            ZonedDateTime upperZoned = upperInstant.atZone(zoneId);
            long seconds = ChronoUnit.SECONDS.between(lowerZoned, upperZoned);
            ZonedDateTime medianZoned = lowerZoned.plusSeconds(seconds / 2);
            Instant medianInstant = medianZoned.toInstant();
            return Date.from(medianInstant);
        });
    }

    @Bean
    public Ranges.Printer<Date> dateRangePrinter() {
        return new Ranges.Printer<>(date -> {
            Instant instant = date.toInstant();
            Instant truncated = instant.truncatedTo(ChronoUnit.SECONDS);
            return dateTimeFormatter().format(truncated);
        });
    }

    @Bean
    public Pageable suggestionLimitPageable(@Value("${app.statistics.suggestion-limit}") Integer limit) {
        return PageRequest.of(0, limit);
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
                registry.addConverter(new GitRepoToDtoConverter());
                registry.addConverter(new GitRepoDtoToCsvConverter(csvMapper));
                registry.addConverter(new JsonObjectToGitCommitConverter());
                registry.addConverter(new JsonObjectToRateLimitConverter());
                registry.addConverter(new JsonObjectToErrorResponseConverter());
                registry.addConverter(new JsonObjectToGitRepoMetricConverter());
                registry.addConverter(new SearchParameterDtoToGitRepoSearchConverter());
                registry.addConverter(new StringToContactsConverter());
                registry.addConverter(new StringToGitExceptionConverter());
                registry.addConverter(new StringToJsonElementConverter());
                registry.addConverter(new StringToJsonObjectConverter(gson()));
                registry.addConverter(new StringToLicensesConverter());
                registry.addConverter(new StringToNavigationLinksConverter());
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
