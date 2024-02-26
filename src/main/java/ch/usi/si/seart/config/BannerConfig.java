package ch.usi.si.seart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class BannerConfig {

    @Bean
    public Banner banner(@Value("${spring.banner.location}") Resource resource) {
        if (resource.exists()) return new ResourceBanner(resource);
        else return (environment, sourceClass, out) -> {
        };
    }
}
