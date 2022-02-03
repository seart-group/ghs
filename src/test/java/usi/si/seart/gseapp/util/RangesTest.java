package usi.si.seart.gseapp.util;

import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Test;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.BinaryOperator;

public class RangesTest {

    @Test
    public void testBuild() {
        Assert.assertEquals(Range.all(), Ranges.build(null, null));
        Assert.assertEquals(Range.atLeast(5), Ranges.build(5, null));
        Assert.assertEquals(Range.atMost(10), Ranges.build(null, 10));
        Assert.assertEquals(Range.closed(5, 10), Ranges.build(5, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildException() {
        Ranges.build(5, 1);
    }

    @Test
    public void testSplit() {
        BinaryOperator<Long> average = (a, b) -> (a + b)/2;

        List<Range<Long>> ranges = Ranges.split(Range.closed(2L, 10L), average);
        Assert.assertEquals(2, ranges.size());
        Assert.assertEquals(Range.closed(2L, 6L), ranges.get(0));
        Assert.assertEquals(Range.closed(6L, 10L), ranges.get(1));

        ranges = Ranges.split(Range.closed(2L, 2L), average);
        Assert.assertEquals(1, ranges.size());
        Assert.assertEquals(Range.closed(2L, 2L), ranges.get(0));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("5..10", Ranges.toString(Range.closed(5, 10), NumberFormat.getInstance()));
        Assert.assertEquals("5..10", Ranges.toString(Range.closed(5L, 10L), NumberFormat.getInstance()));
        Assert.assertEquals("5..", Ranges.toString(Range.atLeast(5), NumberFormat.getInstance()));
        Assert.assertEquals("..5", Ranges.toString(Range.atMost(5), NumberFormat.getInstance()));
        Assert.assertEquals("", Ranges.toString(Range.all(), NumberFormat.getInstance()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1, 0, 0);
        Date lower = calendar.getTime();
        calendar.set(2022, Calendar.JANUARY, 2, 0, 0);
        Date upper = calendar.getTime();
        Range<Date> dateRange = Range.closed(lower, upper);
        Assert.assertEquals("2022-01-01..2022-01-02", Ranges.toString(dateRange, new SimpleDateFormat("yyyy-MM-dd")));
        Assert.assertEquals(
                "2022-01-01T00:00..2022-01-02T00:00",
                Ranges.toString(dateRange, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"))
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToStringException() {
        Ranges.toString(Range.closed(new Date(), new Date()), NumberFormat.getInstance());
    }
}