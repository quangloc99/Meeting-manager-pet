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

    static {
        calendarFieldMap.put("year", Calendar.YEAR);
        calendarFieldMap.put("month", Calendar.MONTH);
        calendarFieldMap.put("date", Calendar.DATE);
        calendarFieldMap.put("hour", Calendar.HOUR);
        calendarFieldMap.put("minute", Calendar.MINUTE);
        calendarFieldMap.put("second", Calendar.SECOND);
    }

}
