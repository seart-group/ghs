package ch.usi.si.seart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.util.Arrays;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI(Info info) {
        return new OpenAPI().info(info);
    }

    @Bean
    Info info(BuildProperties buildProperties, Contact contact, License license) {
        String title = buildProperties.get("name");
        String description = buildProperties.get("description");
        String version = buildProperties.get("version");
        return new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(contact)
                .license(license);
    }

    @Bean
    @SuppressWarnings("ConstantConditions")
    Contact contact(BuildProperties buildProperties, ConversionService conversionService) {
        Contact[] contacts = conversionService.convert(buildProperties.get("developers"), Contact[].class);
        return Arrays.stream(contacts)
                .findFirst()
                .orElse(new Contact());
    }

    @Bean
    @SuppressWarnings("ConstantConditions")
    License license(BuildProperties buildProperties, ConversionService conversionService) {
        License[] licenses = conversionService.convert(buildProperties.get("licenses"), License[].class);
        return Arrays.stream(licenses)
                .findFirst()
                .orElse(new License());
    }
}
