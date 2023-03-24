package usi.si.seart.exception;

/**
 * A runtime exception to be thrown when an error occurs on invocation or during runtime of a terminal process.
 */
public class TerminalExecutionException extends RuntimeException {
    public TerminalExecutionException(String message) {
        super(message);
    }

    public TerminalExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TerminalExecutionException(Throwable cause) {
        super(cause);
    }
}
