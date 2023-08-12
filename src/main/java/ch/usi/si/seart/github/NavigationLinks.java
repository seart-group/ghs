package ch.usi.si.seart.github;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NavigationLinks {

    URL first;
    URL previous;
    URL next;
    URL last;

    public Long getFirstPage() {
        return getPageNumber(first);
    }

    public Long getPreviousPage() {
        return getPageNumber(previous);
    }

    public Long getNextPage() {
        return getPageNumber(next);
    }

    public Long getLastPage() {
        return getPageNumber(last);
    }

    private static Long getPageNumber(URL url) {
        return Optional.ofNullable(url)
                .map(URL::getQuery)
                .map(NavigationLinks::splitParameters)
                .map(parameters -> parameters.get("page"))
                .map(Long::parseLong)
                .orElse(null);
    }

    private static Map<String, String> splitParameters(String query) {
        return Splitter.on("&")
                .withKeyValueSeparator("=")
                .split(query);
    }
}
