package ru.ifmo.se.s267880.lab5;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tran Quang Loc
 */
public class Helper {
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

}
