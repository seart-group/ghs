package com.dabico.gseapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
    public static Date fromGitDateString(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Universal"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex){
            return null;
        }
    }
}
