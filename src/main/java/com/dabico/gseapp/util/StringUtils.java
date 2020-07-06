package com.dabico.gseapp.util;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class StringUtils {
    public static String removeFromStart(String str, int n) {
        if (!isEmpty(str)) {
            return str.length() >= n ? str.substring(n) : str;
        } else {
            return str;
        }
    }

    public static String removeFromEnd(String str, int n) {
        if (!isEmpty(str)) {
            return str.length() >= n ? str.substring(0, str.length() - n) : str;
        } else {
            return str;
        }
    }

    public static String removeFromStartAndEnd(String str, int m, int n){
        if (!isEmpty(str)) {
            return (str.length() >= m && str.length() >= n) ? str.substring(m, str.length() - n) : str;
        } else {
            return str;
        }
    }
}
