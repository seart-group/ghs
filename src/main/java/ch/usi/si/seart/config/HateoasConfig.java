package ch.usi.si.seart.config;

import ch.usi.si.seart.hateoas.DownloadLinkBuilder;
import ch.usi.si.seart.hateoas.LinkBuilder;
import ch.usi.si.seart.hateoas.PaginationLinkBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

@Configuration
public class HateoasConfig {

    @Bean
    public LinkBuilder<Page<?>> searchLinkBuilder() {
        return new PaginationLinkBuilder();
    }

    @Bean
    public LinkBuilder<Void> downloadLinkBuilder() {
        return new DownloadLinkBuilder();
    }
}
