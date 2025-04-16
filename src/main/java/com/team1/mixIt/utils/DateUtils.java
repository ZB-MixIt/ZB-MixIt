package com.team1.mixIt.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter YYMMDDFormat = DateTimeFormatter.ofPattern("yyMMdd");
    private DateUtils() {}

    public static String yyMMdd(LocalDate dateTime) {
        return dateTime.format(YYMMDDFormat);
    }
}
