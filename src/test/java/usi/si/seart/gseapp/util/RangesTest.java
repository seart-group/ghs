package usi.si.seart.gseapp.util;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.BinaryOperator;

public class RangesTest {

    @Test
    public void testBuild() {
        Assertions.assertEquals(Range.all(), Ranges.build(null, null));
        Assertions.assertEquals(Range.atLeast(5), Ranges.build(5, null));
        Assertions.assertEquals(Range.atMost(10), Ranges.build(null, 10));
        Assertions.assertEquals(Range.closed(5, 10), Ranges.build(5, 10));
    }

    @Test
    public void testBuildException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Ranges.build(5, 1));
    }

    @Test
    public void testSplit() {
        BinaryOperator<Long> average = (a, b) -> (a + b)/2;

        List<Range<Long>> ranges = Ranges.split(Range.closed(2L, 10L), average);
        Assertions.assertEquals(2, ranges.size());
        Assertions.assertEquals(Range.closed(2L, 6L), ranges.get(0));
        Assertions.assertEquals(Range.closed(6L, 10L), ranges.get(1));

        ranges = Ranges.split(Range.closed(2L, 2L), average);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(Range.closed(2L, 2L), ranges.get(0));
    }

    @Test
    public void testToString() {
        Assertions.assertEquals("5..10", Ranges.toString(Range.closed(5, 10), NumberFormat.getInstance()));
        Assertions.assertEquals("5..10", Ranges.toString(Range.closed(5L, 10L), NumberFormat.getInstance()));
        Assertions.assertEquals("5..", Ranges.toString(Range.atLeast(5), NumberFormat.getInstance()));
        Assertions.assertEquals("..5", Ranges.toString(Range.atMost(5), NumberFormat.getInstance()));
        Assertions.assertEquals("", Ranges.toString(Range.all(), NumberFormat.getInstance()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1, 0, 0);
        Date lower = calendar.getTime();
        calendar.set(2022, Calendar.JANUARY, 2, 0, 0);
        Date upper = calendar.getTime();
        Range<Date> dateRange = Range.closed(lower, upper);
        Assertions.assertEquals("2022-01-01..2022-01-02", Ranges.toString(dateRange, new SimpleDateFormat("yyyy-MM-dd")));
        Assertions.assertEquals(
                "2022-01-01T00:00..2022-01-02T00:00",
                Ranges.toString(dateRange, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"))
        );
    }

    @Test
    public void testToStringException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Ranges.toString(Range.closed(new Date(), new Date()), NumberFormat.getInstance())
        );
    }
}