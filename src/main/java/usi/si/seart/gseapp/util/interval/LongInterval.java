package usi.si.seart.gseapp.util.interval;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.javatuples.Pair;

@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class LongInterval extends Interval<Long>{
    public Pair<LongInterval,LongInterval> splitInterval() {
        if (start.equals(end)){ return null; }
        Long median = (start + end)/2;
        LongInterval firstInterval  = LongInterval.builder().start(start).end(median).build();
        LongInterval secondInterval = LongInterval.builder().start(median).end(end).build();
        return new Pair<>(firstInterval,secondInterval);
    }
}
