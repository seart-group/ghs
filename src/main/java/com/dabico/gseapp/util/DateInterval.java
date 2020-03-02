package com.dabico.gseapp.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.javatuples.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.dabico.gseapp.util.DateUtils.*;
import static org.apache.commons.lang3.time.DateUtils.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DateInterval {

    Date start;
    Date end;

    public DateInterval(Date start, Date end){
        this.start = setInitDay(start);
        this.end   = setInitDay(end);
    }

    public Pair<DateInterval,DateInterval> splitInterval(){
        if (isSameDay(this.start,this.end)){
            return null;
        }
        Date median = setInitDay(new Date((start.getTime() + end.getTime())/2));
        DateInterval firstInterval  = new DateInterval(start,median);
        DateInterval secondInterval = new DateInterval(median,end);
        return new Pair<>(firstInterval,secondInterval);
    }

    @Override
    public String toString(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(this.start) + ".."  + simpleDateFormat.format(this.end);
    }

    @Override
    public boolean equals(Object obj){
        return (obj == this) ||
               ((obj instanceof DateInterval) &&
                (this.start == ((DateInterval) obj).start) &&
                (this.end == ((DateInterval) obj).end));
    }
}
