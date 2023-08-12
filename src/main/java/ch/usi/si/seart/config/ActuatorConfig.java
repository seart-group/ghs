package ch.usi.si.seart.config;

import ch.usi.si.seart.github.GitHubAPIHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    @Bean("gitHubApi")
    public HealthIndicator gitHubApiHealthIndicator() {
        return new GitHubAPIHealthIndicator();
    }
}
