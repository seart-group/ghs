package ch.usi.si.seart.config;

import ch.usi.si.seart.converter.ExportFormatToJsonFactoryConverter;
import ch.usi.si.seart.converter.GitRepoToDtoConverter;
import ch.usi.si.seart.converter.JsonObjectToGitCommitConverter;
import ch.usi.si.seart.converter.JsonObjectToGitRepoMetricConverter;
import ch.usi.si.seart.converter.SearchParameterDtoToSpecificationConverter;
import ch.usi.si.seart.converter.StringToContactsConverter;
import ch.usi.si.seart.converter.StringToExportFormatConverter;
import ch.usi.si.seart.converter.StringToGitExceptionConverter;
import ch.usi.si.seart.converter.StringToLicensesConverter;
import ch.usi.si.seart.web.filter.Slf4jMDCLoggingFilter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.ForwardedHeaderFilter;
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
        registry.addConverter(new ExportFormatToJsonFactoryConverter());
        registry.addConverter(new GitRepoToDtoConverter());
        registry.addConverter(new JsonObjectToGitCommitConverter());
        registry.addConverter(new JsonObjectToGitRepoMetricConverter());
        registry.addConverter(new SearchParameterDtoToSpecificationConverter());
        registry.addConverter(new StringToContactsConverter());
        registry.addConverter(new StringToExportFormatConverter());
        registry.addConverter(new StringToGitExceptionConverter());
        registry.addConverter(new StringToLicensesConverter());
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        return bean;
    }

    @Bean
    public FilterRegistrationBean<Slf4jMDCLoggingFilter> slf4jMDCLoggingFilter() {
        FilterRegistrationBean<Slf4jMDCLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new Slf4jMDCLoggingFilter());
        return bean;
    }
}
