package ru.ifmo.se.s267880.lab56.shared;

import ru.ifmo.se.s267880.lab56.client.ConsoleWrapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// An utility class for processing time zone.
public class ZoneUtils {
    public static boolean isValidTimeZone(final String timeZone) {
        // This code got from:
        // https://stackoverflow.com/a/40939623
        final String DEFAULT_GMT_TIMEZONE = "GMT";
        return timeZone.equals(DEFAULT_GMT_TIMEZONE) || !TimeZone.getTimeZone(timeZone).getID().equals(DEFAULT_GMT_TIMEZONE);
    }

    public static String timeZoneToGMTString(TimeZone zone) {
        long minutes = zone.getRawOffset() / 1000 / 60;
        return String.format("GMT%+d:%02d", minutes / 60, (minutes % 60 + 60) % 60);
    }
}
