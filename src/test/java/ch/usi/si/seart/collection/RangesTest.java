package ch.usi.si.seart.collection;

import ch.usi.si.seart.exception.UnsplittableRangeException;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class RangesTest {

    private static final Ranges.Splitter<Long> splitter = new Ranges.Splitter<>((lower, upper) -> (lower + upper)/2);
    private static final Ranges.Printer<Integer> integerPrinter = new Ranges.Printer<>(Object::toString);
    private static final Ranges.Printer<Date> dateprinter = new Ranges.Printer<>(new SimpleDateFormat("yyyy-MM-dd"));
    private static final Ranges.Printer<Date> dateTimePrinter = new Ranges.Printer<>(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));

    @Test
    void testBuild() {
        Assertions.assertEquals(Range.all(), Ranges.builder().build());
        Assertions.assertEquals(Range.atLeast(5), Ranges.<Integer>builder().lower(5).build());
        Assertions.assertEquals(Range.atMost(10), Ranges.<Integer>builder().upper(10).build());
        Assertions.assertEquals(Range.closed(5, 10), Ranges.closed(5, 10));
    }

    @Test
    void testBuildException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Ranges.closed(5, 1));
    }

    @Test
    void testSplit() {
        Pair<Range<Long>, Range<Long>> ranges = splitter.split(Range.closed(2L, 10L));
        Assertions.assertEquals(Range.closed(2L, 6L), ranges.getLeft());
        Assertions.assertEquals(Range.closed(6L, 10L), ranges.getRight());
    }

    @Test
    void testSplitSingleton() {
        Range<Long> singleton = Range.closed(2L, 2L);
        Assertions.assertThrows(UnsplittableRangeException.class, () -> splitter.split(singleton));
    }

    @Test
    void testSplitUnbounded() {
        Range<Long> empty = Range.atLeast(2L);
        Assertions.assertThrows(UnsplittableRangeException.class, () -> splitter.split(empty));
    }

    @Test
    void testIntegerToString() {
        Assertions.assertEquals("5..10", integerPrinter.print(Range.closed(5, 10)));
        Assertions.assertEquals("5..", integerPrinter.print(Range.atLeast(5)));
        Assertions.assertEquals("..5", integerPrinter.print(Range.atMost(5)));
        Assertions.assertEquals("", integerPrinter.print(Range.all()));
    }

    @Test
    void testDateToString() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1, 0, 0);
        Date lower = calendar.getTime();
        calendar.set(2022, Calendar.JANUARY, 2, 0, 0);
        Date upper = calendar.getTime();
        Range<Date> range = Range.closed(lower, upper);
        Assertions.assertEquals("2022-01-01..2022-01-02", dateprinter.print(range));
        Assertions.assertEquals("2022-01-01T00:00..2022-01-02T00:00", dateTimePrinter.print(range));
    }
}