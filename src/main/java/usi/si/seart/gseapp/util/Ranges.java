package usi.si.seart.gseapp.util;

import com.google.common.collect.Range;
import lombok.experimental.UtilityClass;

import java.text.Format;
import java.util.List;
import java.util.function.BinaryOperator;

@SuppressWarnings("rawtypes")
@UtilityClass
public class Ranges {

    public <T extends Comparable> Range<T> build(T lower, T upper){
        Range<T> lowerBound = (lower != null) ? Range.atLeast(lower) : Range.all();
        Range<T> upperBound = (upper != null) ? Range.atMost(upper) : Range.all();
        return lowerBound.intersection(upperBound);
    }

    public <T extends Comparable> List<Range<T>> split(Range<T> range, BinaryOperator<T> medianFunction){
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new IllegalArgumentException("Can not perform split of unbounded range!");
        }

        T lower = range.lowerEndpoint();
        T upper = range.upperEndpoint();

        if (lower.equals(upper)) {
            return List.of(Range.closed(lower, upper));
        }

        T median = medianFunction.apply(lower, upper);
        Range<T> first = Range.closed(lower, median);
        Range<T> second = Range.closed(median, upper);
        return List.of(first, second);
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
