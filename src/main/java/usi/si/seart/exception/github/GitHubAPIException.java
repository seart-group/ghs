package usi.si.seart.exception.github;

import lombok.experimental.StandardException;
import usi.si.seart.github.RestErrorResponse;

@StandardException
public class GitHubAPIException extends RuntimeException {

    public GitHubAPIException(RestErrorResponse errorResponse) {
        this(errorResponse.toString());
    }
}
