package ru.ifmo.se.s267880.lab5;

import JuniorAndCarlson.Meeting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.InputPreprocessor;

import java.text.ParseException;
import java.util.*;

public class MeetingManagerInputPreprocessor extends InputPreprocessor {
    @Override
    public Object preprocess(JsonElement elm, Class cls) {
        if (cls == Meeting.class) {
            try {
                return json2Meeting(elm.getAsJsonObject());
            } catch (ParseException e) {
                System.err.println(e);
                System.err.println("Please using the following format to write date: " + Helper.meetingDateFormat);
                return null;
            } catch (IllegalStateException e) {

            }
        }
        return super.preprocess(elm, cls);
    }

    public static Meeting json2Meeting(JsonObject obj) throws ParseException {
        return new Meeting(obj.get("name").getAsString(), json2Date(obj.get("time")));
    }

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
