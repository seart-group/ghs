package com.dabico.gseapp.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class IntInterval extends Interval<Integer> {

    public IntInterval(Integer start, Integer end) { super(start,end); }

    public Pair<IntInterval,IntInterval> splitInterval(){
        if (start.equals(end)){
            return new Pair<>(new IntInterval(start, end), null);
        }

        Integer median = (start + end)/2;
        IntInterval firstInterval  = new IntInterval(start,median);
        IntInterval secondInterval = new IntInterval(median,end);
        return new Pair<>(firstInterval,secondInterval);
    }

    public String toString(){
        return this.start + ".." + this.end;
    }
}
