package com.dabico.gseapp.util.interval;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class LongInterval extends Interval<Long>{

    public LongInterval(Long start, Long end){ super(start,end); }

    public Pair<LongInterval,LongInterval> splitInterval() {
        if (start.equals(end)){ return null; }
        Long median = (start + end)/2;
        LongInterval firstInterval  = new LongInterval(start,median);
        LongInterval secondInterval = new LongInterval(median,end);
        return new Pair<>(firstInterval,secondInterval);
    }

    public String toString() {
        return this.start + ".." + this.end;
    }
}
