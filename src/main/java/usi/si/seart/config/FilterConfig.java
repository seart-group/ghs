package usi.si.seart.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;
import usi.si.seart.web.filter.RateLimitingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        return bean;
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RateLimitingFilter());
        bean.addUrlPatterns("/r/search", "/r/download/*");
        bean.setOrder(1);
        return bean;
    }
}
