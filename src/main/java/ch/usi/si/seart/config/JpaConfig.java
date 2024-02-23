package ch.usi.si.seart.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "ch.usi.si.seart.model")
@EnableJpaRepositories(value = "ch.usi.si.seart.repository")
public class JpaConfig {
}
