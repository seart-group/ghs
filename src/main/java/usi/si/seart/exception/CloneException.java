package usi.si.seart.exception;

/**
 * An exception to be thrown when an error occurs when cloning a git repository.
 */
public class CloneException extends TerminalExecutionException {
    public CloneException(String message) {
        super(message);
    }

    public CloneException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloneException(Throwable cause) {
        super(cause);
    }
}
