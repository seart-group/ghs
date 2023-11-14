package ch.usi.si.seart.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "ch.usi.si.seart.config.properties")
public class PropertiesConfig {
}
