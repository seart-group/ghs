package usi.si.seart.util;

import com.google.common.collect.Range;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import usi.si.seart.exception.IllegalBoundaryException;
import usi.si.seart.exception.UnsplittableRangeException;

import java.text.Format;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * A utility class for working with Guava's ranges.
 * Provides methods for building, splitting, and
 * formatting ranges as strings.
 *
 * @author Ozren Dabić
 * @see Range
 */
@SuppressWarnings("rawtypes")
@UtilityClass
public class Ranges {

    /**
     * Returns a Range object with lower and upper
     * bounds set based on the provided parameters.
     * If an endpoint parameter is null, then the
     * returned range will be unbounded on that endpoint.
     *
     * @param lower
     * The lower bound of the range.
     * Can be null to specify no lower bound.
     * @param upper
     * The upper bound of the range.
     * Can be null to specify no upper bound.
     * @param <T> The range endpoint type.
     * @return A Range object.
     * @throws IllegalBoundaryException
     * if the lower bound is greater
     * than the upper bound.
     */
    public <T extends Comparable> Range<T> build(T lower, T upper){
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

    /**
     * Splits a given range into two halves
     * using the provided median function.
     *
     * @param range
     * The range to be split.
     * @param medianFunction
     * A function that returns the median
     * value for a given pair of endpoints.
     * @param <T> The range endpoint type.
     * @return
     * A {@link Pair} containing the
     * two halves of the split range.
     * @throws IllegalArgumentException if the range has no upper and/or lower bound.
     * @throws UnsplittableRangeException if range is empty or a singleton.
     */
    public <T extends Comparable> Pair<Range<T>, Range<T>> split(
            Range<T> range, BinaryOperator<T> medianFunction
    ) {
        Objects.requireNonNull(range, "Range must not be null!");
        Objects.requireNonNull(medianFunction, "Median function must not be null!");
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new IllegalArgumentException("Can split unbounded range!");
        }

        T lower = range.lowerEndpoint();
        T upper = range.upperEndpoint();

        if (lower.equals(upper)) {
            String category = (range.isEmpty()) ? "empty" : "singleton";
            throw new UnsplittableRangeException("Can split " + category + " range!");
        }

        T median = medianFunction.apply(lower, upper);
        Range<T> first = Range.closed(lower, median);
        Range<T> second = Range.closed(median, upper);
        return Pair.of(first, second);
    }

    /**
     * Returns a string representation of
     * a range using an endpoint formatter.
     *
     * @param range The range to be formatted.
     * @param formatter The formatter to be used.
     * @param <T> The range endpoint type.
     * @return A string representation of the range.
     * @throws NullPointerException if either parameter is null.
     */
    public <T extends Comparable> String toString(Range<T> range, Format formatter) {
        return toString(range, formatter::format);
    }

    /**
     * Returns a string representation of
     * a range using an endpoint mapping function.
     *
     * @param range The range to be formatted.
     * @param function The transformation function.
     * @param <T> The range endpoint type.
     * @return A string representation of the range.
     * @throws NullPointerException if either parameter is null.
     */
    public <T extends Comparable> String toString(Range<T> range, Function<? super T, String> function) {
        Objects.requireNonNull(range, "Range must not be null!");
        Objects.requireNonNull(function, "Bound mapping function must not be null!");
        StringBuilder builder = new StringBuilder();
        boolean lowerBound = range.hasLowerBound();
        boolean upperBound = range.hasUpperBound();
        if (lowerBound) builder.append(function.apply(range.lowerEndpoint()));
        if (lowerBound || upperBound) builder.append("..");
        if (upperBound) builder.append(function.apply(range.upperEndpoint()));
        return builder.toString();
    }
}
