package usi.si.seart.exception;

import lombok.experimental.StandardException;

/**
 * A runtime exception to be thrown when an error occurs on invocation or during runtime of a terminal process.
 */
@StandardException
public class TerminalExecutionException extends RuntimeException {
}
