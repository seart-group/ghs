package com.dabico.gseapp.util.interval;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.javatuples.Pair;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Interval<T> {

    T start;
    T end;

    public abstract Pair<?,?> splitInterval();

    @Override
    public abstract String toString();

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