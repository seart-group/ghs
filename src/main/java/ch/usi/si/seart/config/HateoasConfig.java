package ch.usi.si.seart.config;

import ch.usi.si.seart.controller.GitRepoController;
import ch.usi.si.seart.dto.SearchParameterDto;
import ch.usi.si.seart.hateoas.DownloadLinkBuilder;
import ch.usi.si.seart.hateoas.LinkBuilder;
import ch.usi.si.seart.hateoas.PaginationLinkBuilder;
import ch.usi.si.seart.web.ExportFormat;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Configuration
public class HateoasConfig {

    private static final String BEAN_EXCEPTION_MESSAGE = "Could not find required method";

    @Bean
    public LinkBuilder<Page<?>> searchLinkBuilder() {
        try {
            Method method = GitRepoController.class.getMethod(
                    "searchRepos",
                    SearchParameterDto.class,
                    Pageable.class,
                    HttpServletRequest.class
            );
            return new PaginationLinkBuilder(method);
        } catch (NoSuchMethodException ex) {
            throw new BeanCreationException(BEAN_EXCEPTION_MESSAGE, ex);
        }
    }

    @Bean
    public LinkBuilder<Void> downloadLinkBuilder() {
        try {
            Method method = GitRepoController.class.getMethod(
                    "downloadRepos",
                    ExportFormat.class,
                    SearchParameterDto.class,
                    HttpServletResponse.class
            );
            return new DownloadLinkBuilder(method);
        } catch (NoSuchMethodException ex) {
            throw new BeanCreationException(BEAN_EXCEPTION_MESSAGE, ex);
        }
    }
}
