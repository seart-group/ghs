package ch.usi.si.seart.config;

import ch.usi.si.seart.repository.support.ExtendedJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "ch.usi.si.seart.model")
@EnableJpaRepositories(
        value = "ch.usi.si.seart.repository",
        repositoryBaseClass = ExtendedJpaRepositoryImpl.class
)
public class JpaConfig {
}
