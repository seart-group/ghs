package ch.usi.si.seart.config;

import ch.usi.si.seart.actuate.info.SpringInfoContributor;
import ch.usi.si.seart.actuate.logging.LogFileWebEndpointExtension;
import ch.usi.si.seart.github.GitHubAPIHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.info.ConditionalOnEnabledInfoContributor;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorFallback;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ActuatorConfig {

    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new InMemoryHttpExchangeRepository();
    }

    @Bean("gitHubApi")
    public HealthIndicator gitHubApiHealthIndicator() {
        return new GitHubAPIHealthIndicator();
    }

    @Bean
    @Order(InfoContributorAutoConfiguration.DEFAULT_ORDER)
    @ConditionalOnEnabledInfoContributor(value = "spring", fallback = InfoContributorFallback.DISABLE)
    public InfoContributor applicationInfoContributor() {
        return new SpringInfoContributor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public LogFileWebEndpointExtension logFileWebEndpointExtension(LogFileWebEndpoint delegate) {
        return new LogFileWebEndpointExtension(delegate);
    }
}
