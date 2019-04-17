package ru.ifmo.se.s267880.lab56.shared;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// An utility class for processing time zone.
public class ZoneUtils {
    static public final Map<Integer, ZoneId> allZoneIds = Collections.unmodifiableMap(generateZonesIdWithIndex());
    static private Map<Integer, ZoneId> generateZonesIdWithIndex() {
        Map<Integer, ZoneId> zones = new HashMap<>();
        ZoneId.getAvailableZoneIds().stream()
                .map(ZoneId::of)
                .sorted(Comparator.comparing(ZoneId::toString))
                .sorted(Comparator.comparing(ZoneUtils::toUTCZoneOffset))
                .forEachOrdered(e -> zones.put(zones.size(), e));
        return zones;
    }

    public static ZoneOffset toUTCZoneOffset(ZoneId id) {
        return LocalDateTime.now().atZone(id).getOffset();
    }

    public static String toUTCZoneOffsetString(ZoneId id) {
        return toUTCZoneOffset(id).toString().replace("Z", "+00:00");
    }

    public static Map<Integer, ZoneId> getZonesBy(Predicate<ZoneId> predicate) {
        return allZoneIds.entrySet().stream()
                .filter(e -> predicate.test(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void printAllZone() {
        printZonesByZoneOffset(allZoneIds);
    }

    public static void printZonesByZoneOffset(Map<Integer, ZoneId> zones) {
        new TreeMap<>(
            zones.entrySet().stream().collect(Collectors.groupingBy(e -> toUTCZoneOffset(e.getValue())))
        ) .forEach((k, v) -> {
            System.out.printf("UTC%s%n", toUTCZoneOffsetString(k));
            v.forEach(e -> System.out.printf("\t%d) %s%n", e.getKey(), e.getValue()));
        }) ;
    }
}
