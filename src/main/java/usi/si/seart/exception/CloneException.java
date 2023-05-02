package usi.si.seart.exception;

import lombok.experimental.StandardException;

/**
 * An exception to be thrown when an error occurs when cloning a git repository.
 */
@StandardException
public class CloneException extends TerminalExecutionException {
}
