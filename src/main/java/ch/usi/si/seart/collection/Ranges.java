package ch.usi.si.seart.collection;

import ch.usi.si.seart.exception.IllegalBoundaryException;
import ch.usi.si.seart.exception.UnsplittableRangeException;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Format;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * A utility class for working with Guava's ranges.
 * Provides utilities for building, splitting, and
 * formatting ranges as strings.
 *
 * @author Ozren Dabić
 * @see Range
 */
@UtilityClass
public class Ranges {

    /**
     * @param range The input range
     * @return Whether the input has bounds on both endpoints.
     */
    public static boolean hasAllBound(Range<?> range) {
        return range.hasLowerBound() && range.hasUpperBound();
    }

    /**
     * @param range The input range
     * @return Whether the input has at least one bound on any endpoint.
     */
    public static boolean hasAnyBound(Range<?> range) {
        return range.hasLowerBound() || range.hasUpperBound();
    }

    /**
     * Convenience method. Equivalent to:
     * <pre>{@code
     *     Ranges.builder().lower(...).upper(...).build();
     * }</pre>
     */
    public <T extends Comparable<T>> Range<T> closed(@Nullable T lower, @Nullable T upper) {
        return new Builder<T>()
                .lower(lower)
                .upper(upper)
                .build();
    }

    /**
     * @param <T> The range endpoint type.
     * @return A {@link Range} builder.
     */
    public <T extends Comparable<T>> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * A fluent API for building ranges.
     * All ranges returned by this builder are <em>closed</em> by default.
     * If either endpoint is null, then the returned range will be unbounded on that endpoint.
     *
     * @param <T> The range endpoint type.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Builder<T extends Comparable<T>> {

        T lower;
        T upper;

        /**
         * Set the lower bound of the range.
         * {@code null} value corresponds to no bound specified.
         *
         * @param lower lower bound of the range.
         */
        public Builder<T> lower(@Nullable T lower) {
            this.lower = lower;
            return this;
        }

        /**
         * Set the upper bound of the range.
         * {@code null} value corresponds to no bound specified.
         *
         * @param upper upper bound of the range.
         */
        public Builder<T> upper(@Nullable T upper) {
            this.upper = upper;
            return this;
        }

        /**
         * Finalize range object construction.
         *
         * @return {@link Range} object.
         * @throws IllegalBoundaryException if the lower bound is greater than the upper bound.
         */
        public Range<T> build() {
            Range<T> lowerBound = (lower != null) ? Range.atLeast(lower) : Range.all();
            Range<T> upperBound = (upper != null) ? Range.atMost(upper) : Range.all();
            try {
                return lowerBound.intersection(upperBound);
            } catch (IllegalArgumentException ignored) {
                String format = "Can not construct range where the lower endpoint (%s) " +
                        "is greater than the upper endpoint (%s)";
                String message = String.format(format, lower, upper);
                throw new IllegalBoundaryException(message);
            }
        }
    }

    /**
     * Utility class for splitting ranges.
     *
     * @param <T> The range endpoint type.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Splitter<T extends Comparable<T>> {

        BinaryOperator<T> function;

        /**
         * @param function A bi-parameter mapping that returns the median value for a given pair of endpoints.
         * @throws NullPointerException if the specified median function is {@code null}.
         */
        public Splitter(@NotNull BinaryOperator<T> function) {
            Objects.requireNonNull(function, "Median function must not be null!");
            this.function = function;
        }

        /**
         * Splits a range into two halves using the provided median function.
         *
         * @param range The range to be split.
         * @return {@link Pair} containing the two halves of the split range.
         * @throws IllegalArgumentException if the range has no upper and/or lower bound.
         * @throws UnsplittableRangeException if range is empty or a singleton.
         * @throws NullPointerException if the range is {@code null}.
         */
        public Pair<Range<T>, Range<T>> split(@NotNull Range<T> range) {
            Objects.requireNonNull(range, "Range must not be null!");
            if (!hasAllBound(range)) {
                throw new UnsplittableRangeException("Can't split unbounded range!");
            }

            T lower = range.lowerEndpoint();
            T upper = range.upperEndpoint();

            if (lower.equals(upper)) {
                String category = (range.isEmpty()) ? "empty" : "singleton";
                throw new UnsplittableRangeException("Can't split " + category + " range!");
            }

            T median = function.apply(lower, upper);
            Range<T> first = Range.closed(lower, median);
            Range<T> second = Range.closed(median, upper);
            return Pair.of(first, second);
        }
    }


    /**
     * Utility class for representing ranges as strings.
     *
     * @param <T> The range endpoint type.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Printer<T extends Comparable<T>> {

        Function<? super T, String> function;

        /**
         * @param function The endpoint printing function.
         * @throws NullPointerException if the specified printing function is {@code null}.
         */
        public Printer(@NotNull Function<? super T, String> function) {
            Objects.requireNonNull(function, "Printing function must not be null!");
            this.function = function;
        }

        /**
         * @param format The formatter specifying how endpoint data is represented in string form.
         * @throws NullPointerException if the specified formatter is {@code null}.
         */
        public Printer(@NotNull Format format) {
            Objects.requireNonNull(format, "Format must not be null!");
            this.function = format::format;
        }

        /**
         * @param range the range to be formatted as string.
         * @return string representation of the range.
         * @throws NullPointerException if the range is {@code null}.
         */
        public String print(@NotNull Range<T> range) {
            Objects.requireNonNull(range, "Range must not be null!");
            StringBuilder builder = new StringBuilder();
            boolean lowerBound = range.hasLowerBound();
            boolean upperBound = range.hasUpperBound();
            if (lowerBound) builder.append(function.apply(range.lowerEndpoint()));
            if (lowerBound || upperBound) builder.append("..");
            if (upperBound) builder.append(function.apply(range.upperEndpoint()));
            return builder.toString();
        }
    }
}
