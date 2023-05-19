package usi.si.seart.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache("labels"),
                new ConcurrentMapCache("languageStatistics"),
                new ConcurrentMapCache("licenses"),
                new ConcurrentMapCache("languages"),
                new ConcurrentMapCache("topics")
        ));
        return cacheManager;
    }
}
