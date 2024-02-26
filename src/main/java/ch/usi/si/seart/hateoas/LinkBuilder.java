package ch.usi.si.seart.hateoas;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
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

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class LinkBuilder<T> {

    @NonNull
    Method method;

    public String getLinks(HttpServletRequest request) {
        return getLinks(request, null);
    }

    public abstract String getLinks(HttpServletRequest request, T argument);

    protected UriTemplate getUriTemplate() {
        WebMvcLinkBuilder linkBuilder = WebMvcLinkBuilder.linkTo(method);
        return UriTemplate.of(linkBuilder.toString());
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
