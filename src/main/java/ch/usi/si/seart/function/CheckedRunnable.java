package ch.usi.si.seart.function;

/**
 * Alternative version of {@link Runnable} that can throw an exception.
 *
 * @param <E> the type of exception that the runnable can throw.
 * @see Runnable
 * @author Ozren Dabić
 */
@FunctionalInterface
public interface CheckedRunnable<E extends Exception> {

    void run() throws E;
}
