package usi.si.seart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OpenAPIConfig {

    String title;
    String description;
    String version;
    License license;
    Contact contact;

    // TODO: 10.04.23 Update this once they enable support for multiple licenses and/or contacts
    @SuppressWarnings("ConstantConditions")
    @Autowired
    public OpenAPIConfig(BuildProperties buildProperties, ConversionService conversionService) {
        this.title = buildProperties.get("name");
        this.description = buildProperties.get("description");
        this.version = buildProperties.get("version");
        License[] licenses = conversionService.convert(buildProperties.get("licenses"), License[].class);
        Contact[] contacts = conversionService.convert(buildProperties.get("developers"), Contact[].class);
        this.license = (licenses.length > 0) ? licenses[0] : null;
        this.contact = (contacts.length > 0) ? contacts[0] : null;
    }

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(contact)
                .license(license);
        return new OpenAPI().info(info);
    }
}
