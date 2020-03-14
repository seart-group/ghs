package com.dabico.gseapp.util.interval;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class IntInterval extends Interval<Integer> {

    public IntInterval(Integer start, Integer end){ super(start,end); }

    public IntInterval(String interval){
        super();
        String[] tokens = interval.split("\\.\\.");
        String startString = tokens[0];
        String endString = tokens[1];
        this.start = Integer.parseInt(startString);
        this.end = Integer.parseInt(endString);
    }

    public Pair<IntInterval,IntInterval> splitInterval(){
        if (start.equals(end)){ return null; }
        Integer median = (start + end)/2;
        IntInterval firstInterval  = new IntInterval(start,median);
        IntInterval secondInterval = new IntInterval(median,end);
        return new Pair<>(firstInterval,secondInterval);
    }

    public String toString(){ return this.start + ".." + this.end; }
}
