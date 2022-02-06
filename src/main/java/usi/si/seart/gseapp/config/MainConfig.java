package usi.si.seart.gseapp.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usi.si.seart.gseapp.converter.AccessTokenToDtoConverter;
import usi.si.seart.gseapp.converter.CrawlJobToDtoConverter;
import usi.si.seart.gseapp.converter.GitRepoToDtoConverter;
import usi.si.seart.gseapp.converter.SupportedLanguageToDtoConverter;

import java.util.List;

@Configuration
@EnableCaching
public class MainConfig {
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

    /**
     * By default, Spring Boot will use just a single thread for all scheduled tasks to run.
     * Since we have three scheduler jobs:
     *
     * <ul>
     *     <li>Crawler</li>
     *     <li>CleanUp</li>
     *     <li>CacheEvict</li>
     * </ul>
     *
     * We configure the threads here.
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        return threadPoolTaskScheduler;
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache("labels"),
                new ConcurrentMapCache("languageStatistics"),
                new ConcurrentMapCache("licenses"),
                new ConcurrentMapCache("languages")
        ));
        return cacheManager;
    }
}
