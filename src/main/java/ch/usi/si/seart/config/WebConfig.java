package ch.usi.si.seart.config;

import ch.usi.si.seart.converter.GitRepoToDtoConverter;
import ch.usi.si.seart.converter.JsonObjectToErrorResponseConverter;
import ch.usi.si.seart.converter.JsonObjectToGitCommitConverter;
import ch.usi.si.seart.converter.JsonObjectToGitRepoMetricConverter;
import ch.usi.si.seart.converter.SearchParameterDtoToSpecificationConverter;
import ch.usi.si.seart.converter.StringToContactsConverter;
import ch.usi.si.seart.converter.StringToGitExceptionConverter;
import ch.usi.si.seart.converter.StringToLicensesConverter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NotNull final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET")
                .allowedOrigins(
                        "http://localhost:3030",
                        "http://localhost:7030",
                        "https://seart-ghs.si.usi.ch"
                )
                .exposedHeaders("X-Link-Search", "X-Link-Download");
    }

    @Override
    public void addFormatters(@NotNull final FormatterRegistry registry) {
        registry.addConverter(new GitRepoToDtoConverter());
        registry.addConverter(new JsonObjectToGitCommitConverter());
        registry.addConverter(new JsonObjectToErrorResponseConverter());
        registry.addConverter(new JsonObjectToGitRepoMetricConverter());
        registry.addConverter(new SearchParameterDtoToSpecificationConverter());
        registry.addConverter(new StringToContactsConverter());
        registry.addConverter(new StringToGitExceptionConverter());
        registry.addConverter(new StringToLicensesConverter());
    }
}
