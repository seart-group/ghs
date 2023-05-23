package usi.si.seart.util;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Class providing utility methods for working with dates.
 *
 * @author Ozren Dabić
 */
@UtilityClass
public class Dates {

    /**
     * Returns the median of two dates.
     * @param lower the start date.
     * @param upper the end date.
     * @return the date between the start and end dates.
     */
    public Date median(Date lower, Date upper) {
        Objects.requireNonNull(lower, "Lower bound can not be null!");
        Objects.requireNonNull(upper, "Upper bound can not be null!");
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

    /**
     * Parses a date string in the Git date
     * format and returns a Date object.
     *
     * @param date
     * Date string expressed in the
     * "yyyy-MM-dd'T'HH:mm:ss'Z'" format.
     * @return
     * The corresponding Date object,
     * or null if the string cannot be parsed.
     */
    public Date fromGitDateString(String date) {
        Objects.requireNonNull(date, "Date string can not be null!");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Universal"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return null;
        }
    }
}
