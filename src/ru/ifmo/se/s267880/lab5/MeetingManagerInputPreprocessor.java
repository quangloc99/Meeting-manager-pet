package ru.ifmo.se.s267880.lab5;

import JuniorAndCarlson.Meeting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.InputPreprocessor;

import java.text.ParseException;
import java.util.*;

/**
 * An input preprocessor that preprocess Json into Meeting and Date.
 *
 * @author Tran Quang Loc
 * @see InputPreprocessor
 * @see ru.ifmo.se.s267880.lab5.commandControllerHelper.ReflectionCommandAdder
 */
public class MeetingManagerInputPreprocessor extends InputPreprocessor {
    /**
     * The only function that used to transform user input.
     *
     * @param elm the json that typed by the user.
     * @param inputType the desired class of the output.
     * @return
     * @see InputPreprocessor#preprocess(JsonElement, Class)
     */
    @Override
    public Object preprocess(JsonElement elm, Class inputType) {
        if (inputType == Meeting.class) {
            try {
                return json2Meeting(elm.getAsJsonObject());
            } catch (ParseException e) {
                System.err.println(e);
                System.err.println("Please using the following format to write date: " + Helper.meetingDateFormat);
                return null;
            } catch (IllegalStateException e) {

            }
        }
        return super.preprocess(elm, inputType);
    }

    /**
     * Transform JsonObject into Meeting
     * Json object must have 2 following field:<ul>
     * <li>"name": String - the name of the meeting.</li>
     * <li>"time": Date - the meeting time.</li>
     * </ul>
     *
     * @param obj the object needed to be transformed
     * @throws ParseException
     * @see #json2Date(JsonElement)
     */
    public static Meeting json2Meeting(JsonObject obj) throws ParseException {
        return new Meeting(obj.get("name").getAsString(), json2Date(obj.get("time")));
    }

    /**
     * Transform JsonElement into Date.
     * JsonElement can be: <ul>
     *     <li>[int, int, int, int, int, int. From left to right, the values are corresponding,
     *     to Year, Month, Date, Hour, Minute and Second. The array can be shorter, in that case the current time's value
     *     will fill them up. </li>
     *     <li>String with format "yyyy/MM/dd HH:mm:ss"</li>
     *     <li>Object that has fiels: "year", "month", "date", "hour", "minute", "second" and each field must be a int.
     *     The missing field will be filled up like above.</li>
     * </ul>
     * @param elm the json element
     * @return
     * @throws ParseException
     */
    public static Date json2Date(JsonElement elm) throws ParseException {
        if (elm.isJsonArray()) {
            JsonArray arr = elm.getAsJsonArray();
            int[] fields = {0, 0, 0, 0, 0, 0};
            for (int i = 0; i < fields.length && i < arr.size(); ++i) {
                fields[i] = arr.get(i).getAsInt();
            }
            return new GregorianCalendar(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]).getTime();
        } else if (elm.isJsonObject()) {
            JsonObject obj = elm.getAsJsonObject();
            GregorianCalendar cal = new GregorianCalendar();
            Helper.calendarFieldMap.forEach((k, v) -> {
                if (obj.has(k)) cal.set(v, obj.get(k).getAsInt());
            });
            return cal.getTime();
        } else {
            return Helper.meetingDateFormat.parse(elm.getAsString());
        }
    }
}
