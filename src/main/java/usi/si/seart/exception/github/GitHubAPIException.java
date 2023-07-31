package usi.si.seart.exception.github;

import lombok.experimental.StandardException;
import usi.si.seart.github.ErrorResponse;

@StandardException
public class GitHubAPIException extends RuntimeException {

    public GitHubAPIException(ErrorResponse errorResponse) {
        this(errorResponse.toString());
    }
}
