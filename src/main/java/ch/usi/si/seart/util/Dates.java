package ch.usi.si.seart.util;

import lombok.experimental.UtilityClass;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class providing utility methods for working with dates.
 *
 * @author Ozren Dabić
 */
@UtilityClass
public class Dates {

    private static final Date MYSQL_MAX;

    private static final DateFormat FORMAT;

    static {
        FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone zone = TimeZone.getTimeZone("Universal");
        FORMAT.setTimeZone(zone);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(zone);
        calendar.set(Calendar.YEAR, 9999);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        MYSQL_MAX = calendar.getTime();
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
        try {
            Date parsed = FORMAT.parse(date);
            return parsed.after(MYSQL_MAX) ? MYSQL_MAX : parsed;
        } catch (ParseException | NullPointerException ex) {
            return null;
        }
    }
}
