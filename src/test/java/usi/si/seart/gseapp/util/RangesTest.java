package usi.si.seart.gseapp.util;

import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Test;

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
}