package usi.si.seart.util;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

@UtilityClass
public class Dates {

    public Date median(Date lower, Date upper) {
        ZoneId zoneId = ZoneId.of("UTC");
        Instant lowerInstant = lower.toInstant();
        Instant upperInstant = upper.toInstant();
        ZonedDateTime lowerZoned = lowerInstant.atZone(zoneId);
        ZonedDateTime upperZoned = upperInstant.atZone(zoneId);
        long seconds = ChronoUnit.SECONDS.between(lowerZoned, upperZoned);
        ZonedDateTime medianZoned = lowerZoned.plusSeconds(seconds / 2);
        Instant medianInstant = medianZoned.toInstant();
        return Date.from(medianInstant);
    }

    public Date fromGitDateString(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Universal"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex){
            return null;
        }
    }
}
