package ru.ifmo.se.s267880.lab56;

import ru.ifmo.se.s267880.lab56.shared.BuildingLocation;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CannotPreprocessInputException;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.JsonBasicInputPreprocessor;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

/**
 * An input preprocessor that preprocess Json into Meeting and Date.
 *
 * @author Tran Quang Loc
 * @see JsonBasicInputPreprocessor
 * @see ReflectionCommandAdder
 */
public class MeetingManagerInputPreprocessorJson extends JsonBasicInputPreprocessor {
    /**
     * The only function that used to transform user input.
     *
     * @param elm the json that typed by the user.
     * @param inputType the desired class of the output.
     * @see JsonBasicInputPreprocessor#preprocessJson(JsonElement, Class)
     * @throws CannotPreprocessInputException when trying to tranform the input into Date or Meeting and the date object has wrong format. For the orthe types, see {@link JsonBasicInputPreprocessor#preprocessJson(JsonElement, Class)}
     */
    @Override
    protected Object preprocessJson(JsonElement elm, Class inputType) throws CannotPreprocessInputException {
        if (inputType == Meeting.class) {
            try {
                return json2Meeting(elm.getAsJsonObject());
            } catch (Exception e) {
                throw new CannotPreprocessInputException(e.getMessage());
            }
        }
        if (inputType == Date.class) {
            return json2Date(elm);
        }
        return super.preprocessJson(elm, inputType);
    }

    /**
     * Transform JsonObject into Meeting
     * Json object must have 2 following field:<ul>
     * <li>"name": a String represents the name of the meeting. This field is required</li>
     * <li>"time": an object/string represents the meeting time. Default value is the current moment.</li>
     * <li>"duration": an object represents the duration of the meeting. Default is 1 hour.</li>
     * <li>"location": an object represents the meeting's location. Default is [1, 1]</li>
     * </ul>
     *
     * @param obj the object needed to be transformed
     * @throws ParseException
     * @see #json2Date(JsonElement)
     */
    public static Meeting json2Meeting(JsonObject obj) throws CannotPreprocessInputException {
        try {
            return new Meeting(
                    obj.get("name").getAsString(),
                    obj.has("duration") ? json2Duration(obj.get("duration")) : Duration.ofHours(1),
                    obj.has("location") ? json2BuildingLocation(obj.get("location")) : new BuildingLocation(1, 1),
                    obj.has("time") ? json2Date(obj.get("time")) : new Date()
            );
        } catch (Exception e) {
            throw new CannotPreprocessInputException(e.getMessage());
        }
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
    public static Date json2Date(JsonElement elm) throws CannotPreprocessInputException {
        try {
            if (elm.isJsonArray()) {
                JsonArray arr = elm.getAsJsonArray();
                int[] fields = {0, 0, 0, 0, 0, 0};
                for (int i = 0; i < fields.length && i < arr.size(); ++i) {
                    fields[i] = arr.get(i).getAsInt();
                }
                return new GregorianCalendar(fields[0], Helper.monthMap.get(fields[1]), fields[2], fields[3], fields[4], fields[5]).getTime();
            } else if (elm.isJsonObject()) {
                JsonObject obj = elm.getAsJsonObject();
                GregorianCalendar cal = new GregorianCalendar();
                Helper.calendarFieldMap.forEach((k, v) -> {
                    if (obj.has(k)) {
                        if (k.equals("month"))
                            cal.set(v, Helper.monthMap.get(obj.get(k).getAsInt()));
                        else
                            cal.set(v, obj.get(k).getAsInt());
                    } else cal.set(v, 0);
                });
                return cal.getTime();
            } else {
                return Helper.meetingDateFormat.parse(elm.getAsString());
            }
        } catch (Exception e) {
            throw new CannotPreprocessInputException(e.getMessage());
        }
    }

    /**
     * Transform JsonElement into Duration.
     * JsonElement can only be an integer represting the number of minutes that the meeting will last.
     * @param elm
     * @return
     * @throws CanonicalizationMethod
     */
    public static Duration json2Duration(JsonElement elm) throws CannotPreprocessInputException {
        try {
            return Duration.ofMinutes(elm.getAsInt());
        } catch (Exception e) {
            throw new CannotPreprocessInputException(e.getMessage());
        }
    }

    /**
     * Transform JsonElement into location.
     * Json element can have one of the following forms: <ul>
     * <li>Representative object with the following properties: <ul>
     *      <li>"building": an integer represents the building number. Default is 1.</li>
     *      <li>"floor": an integer represents the floor number of the location. Default is 1</li>
     * </ul>
     * <li>Array with 2 integers. The first represents the building number and the second represents the floor number.</li>
     *
     * </li>
     * </ul>
     *
     * @param elm
     * @return
     * @throws CannotPreprocessInputException
     */
    public static BuildingLocation json2BuildingLocation(JsonElement elm) throws CannotPreprocessInputException {
        try {
            if (elm.isJsonArray()) {
                JsonArray arr = elm.getAsJsonArray();
                while (arr.size() < 2) {
                    arr.add(1);
                }
                return new BuildingLocation(arr.get(0).getAsInt(), arr.get(1).getAsInt());
            } else {
                JsonObject obj = elm.getAsJsonObject();
                return new BuildingLocation(
                        obj.has("building") ? obj.get("building").getAsInt() : 1,
                        obj.has("floor") ? obj.get("floor").getAsInt() : 1
                );
            }
        } catch (Exception e) {
            throw new CannotPreprocessInputException(e.getMessage());
        }
    }
}
