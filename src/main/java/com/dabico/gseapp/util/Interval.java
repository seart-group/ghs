package com.dabico.gseapp.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}