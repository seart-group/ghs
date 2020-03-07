package com.dabico.gseapp.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class DateInterval extends Interval<Date> {

    public DateInterval(Date start, Date end) { super(start,end); }

    public Pair<DateInterval,DateInterval> splitInterval(){
        if (start.equals(end)){
            return new Pair<>(new DateInterval(start, end), null);
        }

        Date median = new Date((start.getTime() + end.getTime())/2);
        DateInterval firstInterval  = new DateInterval(start,median);
        DateInterval secondInterval = new DateInterval(median,end);
        return new Pair<>(firstInterval,secondInterval);
    }

    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return dateFormat.format(start) + ".."  + dateFormat.format(end);
    }
}
