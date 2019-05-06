package ru.ifmo.se.s267880.lab56.shared;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Tran Quang Loc
 */
public class Helper {
    /**
     * The default date format for <i>almost</i> Date object.
     */
    public static final DateTimeFormatter meetingDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");

    public static String join(CharSequence delimitor, Object[] obj) {
        String [] str = new String[obj.length];
        for (int i = 0; i < obj.length; ++i) {
            str[i] = obj[i].toString();
        }
        return String.join(delimitor, str);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
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
            throw new NullPointerException("ByteArrayOutputStream thrown");
        }
    }

    public static String TEMP_FILE_PREFIX = "meeting_manager_temp";
    public static File TEMP_DIR = new File(".temp/");
    public static File createTempFile() throws IOException {
        if (!TEMP_DIR.exists()) {
            if (!TEMP_DIR.mkdir()) {
                throw new IOException("cannot create temp file");
            }
        }
        File res = File.createTempFile(TEMP_FILE_PREFIX, null, TEMP_DIR);
        res.deleteOnExit();
        return res;
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

