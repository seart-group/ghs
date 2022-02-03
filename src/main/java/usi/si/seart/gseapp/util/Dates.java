package usi.si.seart.gseapp.util;

import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@UtilityClass
public class Dates {
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
