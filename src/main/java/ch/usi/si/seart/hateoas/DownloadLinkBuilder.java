package ch.usi.si.seart.hateoas;

import ch.usi.si.seart.controller.GitRepoController;
import ch.usi.si.seart.dto.SearchParameterDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

@Component("downloadLinkBuilder")
@AllArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DownloadLinkBuilder extends LinkBuilder<Void> {

    @Qualifier("exportFormats")
    Set<String> exportFormats;

    @Override
    protected Class<?> getControllerClass() {
        return GitRepoController.class;
    }

    @Override
    @SneakyThrows(NoSuchMethodException.class)
    protected Method getControllerMethod() {
        return getControllerClass().getMethod(
                "downloadRepos",
                String.class,
                SearchParameterDto.class,
                HttpServletResponse.class
        );
    }

    @Override
    public String getLinks(HttpServletRequest request, Void ignored) {
        MultiValueMap<String, String> parameters = extractParameters(request);
        UriTemplate template = getUriTemplate();
        return exportFormats.stream()
                .map(format -> {
                    URI base = template.expand(format);
                    URI uri = UriComponentsBuilder.fromUri(base)
                            .queryParams(parameters)
                            .replaceQueryParam("page")
                            .replaceQueryParam("sort")
                            .build(true)
                            .toUri();
                    Link link = Link.of(uri.toString(), format);
                    return link.toString();
                })
                .collect(Collectors.joining(","));
    }
}
