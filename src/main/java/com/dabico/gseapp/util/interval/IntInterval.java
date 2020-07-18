package com.dabico.gseapp.util.interval;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.javatuples.Pair;

@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class IntInterval extends Interval<Integer> {
    public Pair<IntInterval,IntInterval> splitInterval(){
        if (start.equals(end)){ return null; }
        Integer median = (start + end)/2;
        IntInterval firstInterval  = IntInterval.builder().start(start).end(median).build();
        IntInterval secondInterval = IntInterval.builder().start(median).end(end).build();
        return new Pair<>(firstInterval,secondInterval);
    }
}
