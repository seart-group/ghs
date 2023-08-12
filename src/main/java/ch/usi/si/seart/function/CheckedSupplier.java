package ch.usi.si.seart.function;

/**
 * Represents a supplier of results that can throw an exception.
 *
 * @param <T> the type of results supplied by this supplier.
 * @param <E> the type of exception that the supplier can throw.
 * @see java.util.function.Supplier
 * @author Ozren Dabić
 */
@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception> {

    /**
     * Gets a result. May throw an exception.
     *
     * @return a result.
     * @throws E if an exception occurs while getting the result.
     */
    T get() throws E;
}
