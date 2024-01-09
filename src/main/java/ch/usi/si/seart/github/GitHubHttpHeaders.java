package ch.usi.si.seart.github;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class GitHubHttpHeaders {

    public static final String X_GITHUB_REQUEST_ID = "X-GitHub-Request-Id";

    public static final String X_GITHUB_API_VERSION = "X-GitHub-Api-Version";

    public static final String X_RATELIMIT_LIMIT = "X-RateLimit-Limit";

    public static final String X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";

    public static final String X_RATELIMIT_RESET = "X-RateLimit-Reset";

    public static final String X_RATELIMIT_USED = "X-RateLimit-Used";

    public static final String X_RATELIMIT_RESOURCE = "X-RateLimit-Resource";
}
