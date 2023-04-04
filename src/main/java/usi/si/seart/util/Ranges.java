package usi.si.seart.util;

import com.google.common.collect.Range;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import usi.si.seart.exception.UnsplittableRangeException;

import java.text.Format;
import java.util.Objects;
import java.util.function.BinaryOperator;

@SuppressWarnings("rawtypes")
@UtilityClass
public class Ranges {

    public <T extends Comparable> Range<T> build(T lower, T upper){
        Range<T> lowerBound = (lower != null) ? Range.atLeast(lower) : Range.all();
        Range<T> upperBound = (upper != null) ? Range.atMost(upper) : Range.all();
        return lowerBound.intersection(upperBound);
    }

    public <T extends Comparable> Pair<Range<T>, Range<T>> split(
            Range<T> range, BinaryOperator<T> medianFunction
    ) {
        Objects.requireNonNull(medianFunction, "Median function must not be null!");
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new IllegalArgumentException("Can split unbounded range!");
        }

        T lower = range.lowerEndpoint();
        T upper = range.upperEndpoint();

        if (lower.equals(upper)) {
            throw new UnsplittableRangeException("Can split single-value range!");
        }

        T median = medianFunction.apply(lower, upper);
        Range<T> first = Range.closed(lower, median);
        Range<T> second = Range.closed(median, upper);
        return Pair.of(first, second);
    }

    public <T extends Comparable> String toString(Range<T> range, Format formatter) {
        StringBuilder builder = new StringBuilder();
        boolean lowerBound = range.hasLowerBound();
        boolean upperBound = range.hasUpperBound();
        if (lowerBound) builder.append(formatter.format(range.lowerEndpoint()));
        if (lowerBound || upperBound) builder.append("..");
        if (upperBound) builder.append(formatter.format(range.upperEndpoint()));
        return builder.toString();
    }
}
