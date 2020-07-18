package com.dabico.gseapp.util;

import lombok.experimental.UtilityClass;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@UtilityClass
public class StringUtils {
    public String removeFromStart(String str, int n) {
        if (!isEmpty(str)) {
            return str.length() >= n ? str.substring(n) : str;
        } else {
            return str;
        }
    }

    public String removeFromEnd(String str, int n) {
        if (!isEmpty(str)) {
            return str.length() >= n ? str.substring(0, str.length() - n) : str;
        } else {
            return str;
        }
    }

    public String removeFromStartAndEnd(String str, int m, int n){
        if (!isEmpty(str)) {
            return (str.length() >= m && str.length() >= n) ? str.substring(m, str.length() - n) : str;
        } else {
            return str;
        }
    }
}
