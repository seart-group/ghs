package com.dabico.gseapp.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LongUtils {
    public long getLongValue(String input){
        return Long.parseLong(normalizeNumberString(input));
    }

    private String normalizeNumberString(String input){
        return input.trim().replaceAll(",","");
    }
}
