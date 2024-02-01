package com.leomelonseeds.ultimaaddons.utils;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeParser {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm:ss");

    public static String timeUntil(LocalTime time) {
        LocalTime now = LocalTime.now();
        Duration countdown = Duration.between(now, time);
        if (countdown.isNegative())
            countdown = countdown.plusDays(1);

        long hours = countdown.toHours();
        countdown = countdown.minusHours(hours);
        long minutes = countdown.toMinutes();
        countdown = countdown.minusMinutes(minutes);
        long seconds = countdown.toSeconds();

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static LocalTime parse(String str) {
        return LocalTime.parse(str, formatter);
    }

    public static String format(LocalTime time) {
        return time.format(formatter);
    }
}
