package ch.usi.si.seart.github;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Enum providing URLs and URI templates for
 * accessing various resources on the GitHub API.
 * Also provides methods for converting said templates
 * to URLs with the appropriate host and scheme.
 *
 * @author Ozren Dabić
 * @see <a href="https://docs.github.com/en/rest/overview/endpoints-available-for-github-apps">Endpoints available for GitHub Apps</a>
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Endpoint {

    /**
     * Endpoint used for getting a repository.
     *
     * @see <a href="https://docs.github.com/en/rest/repos/repos#get-a-repository">Get a repository</a>
     */
    REPOSITORY(UriTemplate.of("repos/{owner}/{repo}")),

    /**
     * Endpoint used for listing the repository commits.
     *
     * @see <a href="https://docs.github.com/en/rest/commits/commits#list-commits">List commits</a>
     */
    REPOSITORY_COMMITS(UriTemplate.of("repos/{owner}/{repo}/commits")),

    /**
     * Endpoint used for listing the repository branches.
     *
     * @see <a href="https://docs.github.com/en/rest/branches/branches#list-branches">List branches</a>
     */
    REPOSITORY_BRANCHES(UriTemplate.of("repos/{owner}/{repo}/branches")),

    /**
     * Endpoint used for listing repository releases.
     * Does not include regular Git tags that have not
     * been associated with a release.
     *
     * @see <a href="https://docs.github.com/en/rest/releases/releases#list-releases">List releases</a>
     */
    REPOSITORY_RELEASES(UriTemplate.of("repos/{owner}/{repo}/releases")),

    /**
     * Endpoint used for listing repository labels
     * available for use in issues and pull requests.
     *
     * @see <a href="https://docs.github.com/en/rest/issues/labels#list-labels-for-a-repository">List labels for a repository</a>
     */
    REPOSITORY_LABELS(UriTemplate.of("repos/{owner}/{repo}/labels")),

    /**
     * Endpoint used for listing repository languages.
     * The value shown for each language is the number
     * of bytes of code written in that language.
     *
     * @see <a href="https://docs.github.com/en/rest/repos/repos#list-repository-languages">List repository languages</a>
     */
    REPOSITORY_LANGUAGES(UriTemplate.of("repos/{owner}/{repo}/languages")),

    /**
     * Endpoint used for listing repository topics.
     *
     * @see <a href="https://docs.github.com/en/rest/repos/repos#get-all-repository-topics">List repository topics</a>
     */
    REPOSITORY_TOPICS(UriTemplate.of("repos/{owner}/{repo}/topics")),

    /**
     * Endpoint used for listing repository contributors.
     * GitHub identifies contributors by author email address.
     * This endpoint may return information that is a few
     * hours old because the GitHub REST API caches
     * contributor data to improve performance.
     *
     * @see <a href="https://docs.github.com/en/rest/repos/repos#list-repository-contributors">List repository contributors</a>
     */
    REPOSITORY_CONTRIBUTORS(UriTemplate.of("repos/{owner}/{repo}/contributors")),

    /**
     * Endpoint used for listing <em>open</em> repository issues.
     * Although GitHub's REST API considers every pull request an issue,
     * not every issue is a pull request, therefore the endpoint may
     * return both issues and pull requests in the response.
     *
     * @see <a href="https://docs.github.com/en/rest/issues/issues#list-repository-issues">List repository issues</a>
     */
    REPOSITORY_ISSUES(UriTemplate.of("repos/{owner}/{repo}/issues")),

    /**
     * Endpoint used for listing repository pull requests, <em>including</em> drafts.
     *
     * @see <a href="https://docs.github.com/en/rest/pulls/pulls#list-pull-requests">List pull requests</a>
     */
    REPOSITORY_PULLS(UriTemplate.of("repos/{owner}/{repo}/pulls"));

    UriTemplate template;

    private static final String scheme = "https";
    private static final String host = "api.github.com";

    /**
     * GitHub API Root endpoint. Provides hypermedia links to resources accessible in GitHub's REST API.
     *
     * @see <a href="https://docs.github.com/en/rest/meta/meta#github-api-root">GitHub API Root</a>
     */
    public static final URL ROOT = endpointUrl();

    /**
     * Endpoint used for getting the rate limit status for the authenticated user.
     * Accessing this endpoint does not count against your REST API rate limit.
     *
     * @see <a href="https://docs.github.com/en/rest/rate-limit">Rate limit</a>
     */
    public static final URL RATE_LIMIT = endpointUrl("rate_limit");

    /**
     * Endpoint used for performing all GraphQL requests.
     *
     * @see <a href="https://docs.github.com/en/graphql/guides/forming-calls-with-graphql#the-graphql-endpoint">The GraphQL endpoint</a>
     */
    public static final URL GRAPH_QL = endpointUrl("graphql");

    /**
     * Endpoint used for searching for repositories via various criteria.
     *
     * @see <a href="https://docs.github.com/en/rest/search#search-repositories">Search repositories</a>
     */
    public static final URL SEARCH_REPOSITORIES = endpointUrl("search/repositories");

    @SneakyThrows(MalformedURLException.class)
    private static URL endpointUrl() {
        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .build()
                .toUri()
                .toURL();
    }

    @SneakyThrows(MalformedURLException.class)
    private static URL endpointUrl(String endpoint) {
        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .path(endpoint)
                .build()
                .toUri()
                .toURL();
    }

    /**
     * Converts the URI template for this endpoint
     * to a URL with the given arguments replacing
     * any template placeholders.
     *
     * @param args The replacement values for the URI template placeholders.
     * @return A URL object obtained through expanding the URI placeholders.
     */
    @SneakyThrows(MalformedURLException.class)
    public URL toURL(String... args) {
        int expected = template.getVariableNames().size();
        int actual = args.length;
        if (expected > actual) {
            String message = String.format("Insufficient arguments: expected %d but got %d instead", expected, actual);
            throw new IllegalArgumentException(message);
        }
        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .path(template.toString())
                .build((Object[]) args)
                .toURL();
    }
}
