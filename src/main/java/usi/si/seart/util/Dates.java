package usi.si.seart.util;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
