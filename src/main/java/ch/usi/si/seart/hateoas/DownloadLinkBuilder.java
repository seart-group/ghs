package ch.usi.si.seart.hateoas;

import ch.usi.si.seart.web.ExportFormat;
import org.springframework.hateoas.Link;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DownloadLinkBuilder extends LinkBuilder<Void> {

    public DownloadLinkBuilder(Method method) {
        super(method);
    }

    @Override
    public String getLinks(HttpServletRequest request, Void ignored) {
        MultiValueMap<String, String> parameters = extractParameters(request);
        return Stream.of(ExportFormat.values())
                .map(format -> {
                    URI base = getUriTemplate().expand(format);
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
