package usi.si.seart.gseapp.util.interval;

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

    @Override
    public String toString(){
        return this.start + ".." + this.end;
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
