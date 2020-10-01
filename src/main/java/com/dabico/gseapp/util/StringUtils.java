package com.dabico.gseapp.util;

import lombok.experimental.UtilityClass;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@UtilityClass
public class StringUtils {
    public String removeFromStart(String str, int m) {
        return removeFromStartAndEnd(str,m,0);
    }

    public String removeFromEnd(String str, int n) {
        return removeFromStartAndEnd(str,0,n);
    }

    public String removeFromStartAndEnd(String str, int m, int n){
        if (!isEmpty(str) && m >= 0 && n >= 0) {
            return (str.length() >= m && str.length() >= n) ? str.substring(m, str.length() - n) : str;
        } else {
            return str;
        }
    }
}
