package ch.usi.si.seart.hateoas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PaginationLinkBuilder extends LinkBuilder<Page<?>> {

    public PaginationLinkBuilder(Method method) {
        super(method);
    }

    @Override
    public String getLinks(HttpServletRequest request, Page<?> page) {
        MultiValueMap<String, String> parameters = extractParameters(request);
        URI base = getUriTemplate().expand();
        Pageable pageable = page.getPageable();
        int current = pageable.getPageNumber();
        int total = page.getTotalPages();

        List<Link> links = new ArrayList<>();

        links.add(
                Link.of(
                        UriComponentsBuilder.fromUri(base)
                                .queryParams(parameters)
                                .build(true)
                                .toUri()
                                .toString(),
                        IanaLinkRelations.SELF
                )
        );

        if (!page.isFirst()) {
            links.add(
                    Link.of(
                            UriComponentsBuilder.fromUri(base)
                                    .queryParams(parameters)
                                    .replaceQueryParam("page", 0)
                                    .build(true)
                                    .toUri()
                                    .toString(),
                            IanaLinkRelations.FIRST
                    )
            );
        }

        if (page.hasPrevious()) {
            links.add(
                    Link.of(
                            UriComponentsBuilder.fromUri(base)
                                    .queryParams(parameters)
                                    .replaceQueryParam("page", current - 1)
                                    .build(true)
                                    .toUri()
                                    .toString(),
                            IanaLinkRelations.PREV
                    )
            );
        }

        if (page.hasNext()) {
            links.add(
                    Link.of(
                            UriComponentsBuilder.fromUri(base)
                                    .queryParams(parameters)
                                    .replaceQueryParam("page", current + 1)
                                    .build(true)
                                    .toUri()
                                    .toString(),
                            IanaLinkRelations.NEXT
                    )
            );
        }

        if (!page.isLast()) {
            links.add(
                    Link.of(
                            UriComponentsBuilder.fromUri(base)
                                    .queryParams(parameters)
                                    .replaceQueryParam("page", total - 1)
                                    .build(true)
                                    .toUri()
                                    .toString(),
                            IanaLinkRelations.LAST
                    )
            );
        }

        return links.stream()
                .map(Link::toString)
                .collect(Collectors.joining(","));
    }
}
