package ch.usi.si.seart.hateoas;

import ch.usi.si.seart.controller.GitRepoController;
import ch.usi.si.seart.dto.SearchParameterDto;
import ch.usi.si.seart.web.ExportFormat;
import lombok.SneakyThrows;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DownloadLinkBuilder extends LinkBuilder<Void> {

    @Override
    protected Class<?> getControllerClass() {
        return GitRepoController.class;
    }

    @Override
    @SneakyThrows(NoSuchMethodException.class)
    protected Method getControllerMethod() {
        return getControllerClass().getMethod(
                "downloadRepos",
                ExportFormat.class,
                SearchParameterDto.class,
                HttpServletResponse.class
        );
    }

    @Override
    public String getLinks(HttpServletRequest request, Void ignored) {
        MultiValueMap<String, String> parameters = extractParameters(request);
        UriTemplate template = getUriTemplate();
        return Stream.of(ExportFormat.values())
                .map(format -> {
                    URI base = template.expand(format);
                    URI uri = UriComponentsBuilder.fromUri(base)
                            .queryParams(parameters)
                            .replaceQueryParam("page")
                            .replaceQueryParam("sort")
                            .build(true)
                            .toUri();
                    return Link.of(uri.toString(), format.toString());
                })
                .map(Link::toString)
                .collect(Collectors.joining(","));
    }
}
