package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.*;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;

public interface  MiscellaneousCommandHandlers extends CommandHandlers {
    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Command
    @Usage("Show some basic information.")
    void info(HandlerCallback<Map<String, String>> callback);

    @Command("list-timezones")
    @Usage("List all the supported time zones. Note that the list will be very LONG.")
    void listTimeZones(HandlerCallback<List<TimeZone>> callback);

    /**
     * Get all the time zone with index that has ZoneOffset's hour equals to the parameter.
     * @param offsetHour the time zone offset's hour
     * @return a map between a representative index with a ZoneId
     */
    @Command(value="list-timezones", additional = true)
    @Usage("Display all the time zone corresponding the the parameters.\nExample: `list-timezones 3` will list all" +
            " the time zone with the offset \"UTF+3\"")
    void listTimeZones(int offset_hour, HandlerCallback<List<TimeZone>> callback);

    /**
     */
    @Command(value="set-timezone", additional = true)
    @Usage("Set your time zone. There is 2 way to set the time zone:\n" +
            "1) With supported zone id. Use command `list-timezones` for the list of supported time zones. Example:\n" +
            "\tset-timezone Europe/Moscow\n" +
            "\tset-timezone Asia/Ho_Chi_Minh\n" +
            "2) With custom zone id. Use the following syntax to describe the time zone: GMT+/-HH or GMT+/-HH:MM. Example:\n" +
            "\tset-timezone GMT+3\n" +
            "\tset-timezone GMT+7")
    void setTimeZone(String zone_id, HandlerCallback callback);

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(
                MiscellaneousCommandHandlers.class, this, preprocessor
        );
    }
}
