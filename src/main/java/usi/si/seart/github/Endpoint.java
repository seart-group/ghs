package usi.si.seart.github;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import okhttp3.HttpUrl;
import org.springframework.hateoas.UriTemplate;

import java.net.URI;
import java.net.URL;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Endpoint {

    RATE_LIMIT(UriTemplate.of("rate_limit")),

    REPOSITORY(UriTemplate.of("repos/{owner}/{name}")),
    REPOSITORY_COMMITS(UriTemplate.of("repos/{owner}/{name}/commits")),
    REPOSITORY_BRANCHES(UriTemplate.of("repos/{owner}/{name}/branches")),
    REPOSITORY_RELEASES(UriTemplate.of("repos/{owner}/{name}/releases")),
    REPOSITORY_LABELS(UriTemplate.of("repos/{owner}/{name}/labels")),
    REPOSITORY_LANGUAGES(UriTemplate.of("repos/{owner}/{name}/languages")),
    REPOSITORY_CONTRIBUTORS(UriTemplate.of("repos/{owner}/{name}/contributors")),
    REPOSITORY_ISSUES(UriTemplate.of("repos/{owner}/{name}/issues")),
    REPOSITORY_PULLS(UriTemplate.of("repos/{owner}/{name}/pulls")),

    SEARCH(UriTemplate.of("search/repositories"));

    UriTemplate template;

    private static final String scheme = "https";
    private static final String host = "api.github.com";

    public URL toURL() {
        URI uri = template.expand();
        return new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .addPathSegments(uri.toString())
                .build()
                .url();
    }

    public URL toURL(String... args) {
        URI uri = template.expand((Object[]) args);
        return new HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .addEncodedPathSegments(uri.toString())
                .build()
                .url();
    }
}
