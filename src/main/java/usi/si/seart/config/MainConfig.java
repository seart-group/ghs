package usi.si.seart.config;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import okhttp3.OkHttpClient;
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
import usi.si.seart.converter.JsonObjectToGitRepoConverter;
import usi.si.seart.converter.SupportedLanguageToDtoConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Configuration
public class MainConfig {

    CsvMapper csvMapper;

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
                        .exposedHeaders("Links", "Download", "Content-Type", "Transfer-Encoding", "Date");
            }

            @Override
            public void addFormatters(@NotNull final FormatterRegistry registry) {
                 registry.addConverter(new SupportedLanguageToDtoConverter());
                 registry.addConverter(new GitRepoToDtoConverter());
                 registry.addConverter(new JsonObjectToGitRepoConverter());
                 registry.addConverter(new GitRepoDtoToCsvConverter(csvMapper));
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
