package com.dabico.gseapp.util.interval;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PROTECTED)
public class DateInterval extends Interval<Date> {

    public DateInterval(Date start, Date end){ super(start,end); }

    public DateInterval(String interval){
        super();
        String[] tokens = interval.split("\\.\\.");
        String startString = tokens[0];
        String endString = tokens[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            this.start = dateFormat.parse(startString);
            this.end = dateFormat.parse(endString);
        } catch (ParseException ignore){}
    }

    public Pair<DateInterval,DateInterval> splitInterval(){
        if (start.equals(end)){ return null; }
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
