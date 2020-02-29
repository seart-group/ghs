package com.dabico.gseapp.util;

import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.*;

public class DateUtils {
    public static Date setEndDay(Date date) {
        date = setHours(date, 23);
        date = setMinutes(date, 59);
        date = setSeconds(date, 59);
        date = setMilliseconds(date, 999);
        return date;
    }

    public static Date setInitDay(Date date) {
        date = setHours(date, 0);
        date = setMinutes(date, 0);
        date = setSeconds(date, 0);
        date = setMilliseconds(date, 0);
        return date;
    }

    public static Date firstYearDay(int year) {
        Date firstYearDate = new Date();

        firstYearDate = setYears(firstYearDate, year);
        firstYearDate = setMonths(firstYearDate, 0);
        firstYearDate = setDays(firstYearDate, 1);
        firstYearDate = setInitDay(firstYearDate);
        return firstYearDate;
    }

    public static Date lastYearDay(int year) {
        Date lastYearDay = new Date();

        lastYearDay = setYears(lastYearDay, year);
        lastYearDay = setMonths(lastYearDay, 11);
        lastYearDay = setDays(lastYearDay, 31);
        lastYearDay = setEndDay(lastYearDay);

        return lastYearDay;
    }
}
