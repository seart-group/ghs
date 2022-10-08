package usi.si.seart.job;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@EnableScheduling
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheEvictJob {

    CacheManager cacheManager;

    @Scheduled(
            fixedRateString = "${app.cache-evict.scheduling}",
            initialDelayString = "${app.cache-evict.scheduling}"
    )
    public void run() {
        log.info("Clearing caches...");
        cacheManager.getCacheNames()
                .parallelStream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
        log.info("Finished clearing caches.");
    }
}
