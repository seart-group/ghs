package com.dabico.gseapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.apache.commons.lang3.time.DateUtils.*;

public class DateUtils {
    public static Date setInitDay(Date date){
        date = setHours(date, 0);
        date = setMinutes(date, 0);
        date = setSeconds(date, 0);
        date = setMilliseconds(date, 0);
        return date;
    }

    public static Date setEndDay(Date date){
        addDays(date,1);
        return setInitDay(date);
    }

    public static Date firstDayOfYear(int year){
        Date firstYearDate = new Date();
        firstYearDate = setYears(firstYearDate, year);
        firstYearDate = setMonths(firstYearDate, 0);
        firstYearDate = setDays(firstYearDate, 1);
        firstYearDate = setInitDay(firstYearDate);
        return firstYearDate;
    }

    public static Date lastDayOfYear(int year){
        Date lastYearDay = new Date();
        lastYearDay = setYears(lastYearDay, year);
        lastYearDay = setMonths(lastYearDay, 11);
        lastYearDay = setDays(lastYearDay, 31);
        lastYearDay = setEndDay(lastYearDay);
        return lastYearDay;
    }

    public static Date fromGitDateString(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex){
            return null;
        }
    }
}
