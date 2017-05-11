package com.gsu.common.util;

/**
 * Created by cnytync on 09/05/2017.
 */
public class DateUtils {
    public static java.sql.Timestamp getCurrentTimeStamp() {

        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());

    }
}
