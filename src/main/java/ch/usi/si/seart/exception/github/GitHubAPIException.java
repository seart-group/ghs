package ch.usi.si.seart.exception.github;

import ch.usi.si.seart.github.RestErrorResponse;
import lombok.experimental.StandardException;

@StandardException
public class GitHubAPIException extends RuntimeException {

    public GitHubAPIException(RestErrorResponse errorResponse) {
        this(errorResponse.toString());
    }
}
