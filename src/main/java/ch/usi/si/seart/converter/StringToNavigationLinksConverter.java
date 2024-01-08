package ch.usi.si.seart.converter;

import ch.usi.si.seart.github.NavigationLinks;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class StringToNavigationLinksConverter implements Converter<String, NavigationLinks> {

    Converter<String, URL> stringToUrlConverter;

    /*
     * Pattern for matching Link header values of GitHub API responses.
     * https://www.debuggex.com/r/A5_ziqVy-vFaesKK
     */
    private static final Pattern PATTERN = Pattern.compile("(?:,\\s)?<([^>]+)>;\\srel=\"(\\w+)\"");

    @Override
    @NotNull
    public NavigationLinks convert(@NotNull String source) {
        NavigationLinks.NavigationLinksBuilder builder = NavigationLinks.builder();
        Matcher matcher = PATTERN.matcher(source);
        while (matcher.find()) {
            String key = matcher.group(2);
            String value = matcher.group(1);
            URL url = stringToUrlConverter.convert(value);
            switch (key) {
                case "first" -> builder.first(url);
                case "prev" -> builder.previous(url);
                case "next" -> builder.next(url);
                case "last" -> builder.last(url);
                default -> {
                }
            }
        }
        return builder.build();
    }
}
