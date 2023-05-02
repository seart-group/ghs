package usi.si.seart.util;

import lombok.experimental.UtilityClass;
import usi.si.seart.function.CheckedSupplier;

import java.util.Objects;
import java.util.Optional;

/**
 * Class providing utility methods for working with Java's {@link Optional}.
 *
 * @author Ozren Dabić
 */
@UtilityClass
public class Optionals {

    /**
     * Returns an {@code Optional} object that may contain
     * a result returned by the specified supplier,
     * which itself may throw an exception.
     *
     * @param <T> the type of results supplied by this supplier.
     * @param <E> the type of exception that the supplier can throw.
     * @param supplier a checked supplier which may throw an exception.
     * @return an {@code Optional} containing the supplied value,
     * or an empty {@code Optional} if an exception was thrown.
     */
    public <T, E extends Exception> Optional<T> ofThrowable(CheckedSupplier<T, E> supplier) {
        Objects.requireNonNull(supplier, "Supplier must not be null!");
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
