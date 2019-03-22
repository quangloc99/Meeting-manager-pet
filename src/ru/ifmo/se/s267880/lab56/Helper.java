package ru.ifmo.se.s267880.lab56;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Tran Quang Loc
 */
public class Helper {
    public interface FunctionWithException<T, R> {
        R accept(T val) throws Exception;
    }

    public interface ConsumerWithException<T> {
        void accept(T val) throws Exception;
    }

    /**
     * The default date format for <i>almost</i> Date object.
     */
    public static final DateFormat meetingDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Contains the mapping between the string representation of Calendar's fields (YEAR, MONTH) and those contansts.
     * For example {@code calendarFieldMap.get("year") == Calendar.YEAR;}
     */
    static final Map<String, Integer> calendarFieldMap = new HashMap<>();

    /**
     * Contains mapping between human integer's presentation for month and Calendar.{@code <MONTH>} constants.
     */
    static final Map<Integer, Integer> monthMap = new HashMap<>();

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

    public static <T, R> Function<T, R> uncheckedFunction(FunctionWithException<T, R> func) {
        return val -> {
            try {
                return func.accept(val);
            } catch (Exception e) {
                sneakyThrows(e);
                return null;
            }
        };
    }

    public static <T> Consumer<T> uncheckedConsumer(ConsumerWithException<T> consumer) {
        return val -> {
            try {
                consumer.accept(val);
            } catch (Exception e) {
                sneakyThrows(e);
            }
        };
    }

    public static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }
}
