package ch.usi.si.seart.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Represents a {@link CheckedRunnable} that can throw an {@link IOException}.
 */
public interface IOExceptingRunnable extends CheckedRunnable<IOException> {

    /**
     * Returns a wrapped {@link Runnable} that will execute
     * the same operations, albeit throwing an unchecked
     * counterpart of {@link IOException}.
     *
     * @param runnable the runnable to wrap
     * @return an identical runnable that may
     * throw an {@link UncheckedIOException} instead
     */
    static Runnable toUnchecked(CheckedRunnable<IOException> runnable) {
        Objects.requireNonNull(runnable, "Runnable must not be null!");
        return () -> {
            try {
                runnable.run();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };
    }
}
