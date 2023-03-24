package usi.si.seart.exception;

// TODO: Use @StandardException in this package

/**
 * An exception to be thrown when an error occurred while performing static code analysis on a codebase.
 */
public class StaticCodeAnalysisException extends Exception {
    public StaticCodeAnalysisException(String message) {
        super(message);
    }

    public StaticCodeAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public StaticCodeAnalysisException(Throwable cause) {
        super(cause);
    }
}
