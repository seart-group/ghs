package usi.si.seart.gseapp.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MainConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull final CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("GET").allowedOrigins("http://localhost:3030");
            }
        };
    }

    /**
     * By default, Spring Boot will use just a single thread for all scheduled tasks to run.
     * Since we have two scheduler jobs (Crawler, CleanUp), we configure two threads here.
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(2); // 1: Crawler, 2: CleanUp
        return threadPoolTaskScheduler;
    }

}
