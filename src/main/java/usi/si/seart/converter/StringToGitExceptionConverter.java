package usi.si.seart.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import usi.si.seart.exception.git.CheckoutException;
import usi.si.seart.exception.git.GitException;
import usi.si.seart.exception.git.RepositoryNotFoundException;
import usi.si.seart.exception.git.config.InvalidProxyConfigurationException;
import usi.si.seart.exception.git.config.InvalidUsernameException;

public class StringToGitExceptionConverter implements Converter<String, GitException> {

    private static final String EMPTY = "";
    private static final String NOT_FOUND = "not found";
    private static final String LONG_FILE_NAME = "Filename too long";
    private static final String UNRESOLVABLE_HOST = "Could not resolve host: github.com";
    private static final String CHECKOUT_FAILED = "unable to checkout working tree";
    private static final String AUTHENTICATION_REQUIRED =
            "could not read Username for 'https://github.com': No such device or address";

    @Override
    @NonNull
    public GitException convert(@NonNull String source) {
        String fatal = source.lines()
                .filter(line -> line.startsWith("fatal:"))
                .findAny()
                .map(string -> string.substring(7))
                .orElse(EMPTY);
        if (fatal.endsWith(NOT_FOUND))
            return new RepositoryNotFoundException(fatal);
        if (fatal.endsWith(UNRESOLVABLE_HOST))
            return new InvalidProxyConfigurationException(fatal);
        if (fatal.endsWith(LONG_FILE_NAME))
            return new CheckoutException(fatal);
        switch (fatal) {
            case EMPTY:
                return new GitException();
            case AUTHENTICATION_REQUIRED:
                return new InvalidUsernameException(fatal);
            case CHECKOUT_FAILED:
                String error = source.lines()
                        .filter(line -> line.startsWith("error:"))
                        .findAny()
                        .map(string -> string.substring(7))
                        .orElse(null);
                return new CheckoutException(error);
            default:
                return new GitException(fatal);
        }
    }
}
