package com.hzzx.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : HuangZx
 * @date : 2024/6/1 21:37
 */
public class DateUtils {

    public static Date get(String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
