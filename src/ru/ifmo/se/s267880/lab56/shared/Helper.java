package ru.ifmo.se.s267880.lab56.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Tran Quang Loc
 */
public class Helper {
    /**
     * The default date format for <i>almost</i> Date object.
     */
    public static final DateTimeFormatter meetingDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");

    /**
     * Contains the mapping between the string representation of Calendar's fields (YEAR, MONTH) and those contansts.
     * For example {@code calendarFieldMap.get("year") == Calendar.YEAR;}
     */
    public static final Map<String, Integer> calendarFieldMap = new HashMap<>();

    /**
     * Contains mapping between human integer's presentation for month and Calendar.{@code <MONTH>} constants.
     */
    public static final Map<Integer, Integer> monthMap = new HashMap<>();

    static {
        calendarFieldMap.put("year", Calendar.YEAR);
        calendarFieldMap.put("month", Calendar.MONTH);
        calendarFieldMap.put("date", Calendar.DATE);
        calendarFieldMap.put("hour", Calendar.HOUR);
        calendarFieldMap.put("minute", Calendar.MINUTE);
        calendarFieldMap.put("second", Calendar.SECOND);

        monthMap.put(1, Calendar.JANUARY);
        monthMap.put(2, Calendar.FEBRUARY);
        monthMap.put(3, Calendar.MARCH);
        monthMap.put(4, Calendar.APRIL);
        monthMap.put(5, Calendar.MAY);
        monthMap.put(6, Calendar.JUNE);
        monthMap.put(7, Calendar.JULY);
        monthMap.put(8, Calendar.AUGUST);
        monthMap.put(9, Calendar.SEPTEMBER);
        monthMap.put(10, Calendar.OCTOBER);
        monthMap.put(11, Calendar.NOVEMBER);
        monthMap.put(12, Calendar.DECEMBER);
    }

    public static String join(CharSequence delimitor, Object[] obj) {
        String [] str = new String[obj.length];
        for (int i = 0; i < obj.length; ++i) {
            str[i] = obj[i].toString();
        }
        return String.join(delimitor, str);
    }

    public static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }

    public static <K, V> AbstractMap.SimpleEntry<K, V> makePair(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static byte[] serializableToByteArray(Serializable obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            byte[] result = baos.toByteArray();
            oos.close();
            return result;
        } catch (IOException e) {
            assert(false);  // when creating oos with baos, IOException should not be occurs.
        }
        return null;
    }

    public static Class<?> toWrapper(Class<?> clazz) {
        if (!clazz.isPrimitive())
            return clazz;

        if (clazz == Integer.TYPE)
            return Integer.class;
        if (clazz == Long.TYPE)
            return Long.class;
        if (clazz == Boolean.TYPE)
            return Boolean.class;
        if (clazz == Byte.TYPE)
            return Byte.class;
        if (clazz == Character.TYPE)
            return Character.class;
        if (clazz == Float.TYPE)
            return Float.class;
        if (clazz == Double.TYPE)
            return Double.class;
        if (clazz == Short.TYPE)
            return Short.class;
        if (clazz == Void.TYPE)
            return Void.class;

        return clazz;
    }
}

