package usi.si.seart.hateoas;

import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class LinkBuilder<T> {

    public String getLinks(HttpServletRequest request) {
        return getLinks(request, null);
    }

    public abstract String getLinks(HttpServletRequest request, T argument);

    protected abstract Class<?> getControllerClass();

    protected abstract Method getControllerMethod();

    protected UriTemplate getUriTemplate() {
        return UriTemplate.of(
                WebMvcLinkBuilder.linkTo(
                        getControllerClass(),
                        getControllerMethod()
                ).toString()
        );
    }

    protected MultiValueMap<String, String> extractParameters(HttpServletRequest request) {
        return request.getParameterMap()
                .entrySet()
                .stream()
                .map(entry -> {
                    List<String> encoded = Stream.of(entry.getValue())
                            .map(value -> URLEncoder.encode(value, StandardCharsets.UTF_8))
                            .toList();
                    return Map.entry(entry.getKey(), encoded);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> y,
                        LinkedMultiValueMap::new
                ));
    }
}
