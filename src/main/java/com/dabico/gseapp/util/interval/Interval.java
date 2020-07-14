package com.dabico.gseapp.util.interval;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.javatuples.Pair;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Interval<T> {

    T start;
    T end;

    public abstract Pair<?,?> splitInterval();

    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object o){
        return (o == this) ||
              ((o instanceof Interval) &&
               (start.equals(((Interval<?>) o).start)) &&
                 (end.equals(((Interval<?>) o).end)));
    }

    public boolean isBound(){
        return (start != null && end != null);
    }

    public boolean isLowerBound(){
        return (start != null && end == null);
    }

    public boolean isUpperBound(){
        return (start == null && end != null);
    }
}