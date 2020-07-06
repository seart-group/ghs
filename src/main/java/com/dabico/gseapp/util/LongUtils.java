package com.dabico.gseapp.util;

public class LongUtils {
    public static long getLongValue(String input){
        return Long.parseLong(normalizeNumberString(input));
    }

    private static String normalizeNumberString(String input){
        return input.trim().replaceAll(",","");
    }
}
