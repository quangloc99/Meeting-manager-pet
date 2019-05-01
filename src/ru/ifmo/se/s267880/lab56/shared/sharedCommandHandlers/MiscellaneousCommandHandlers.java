package ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.*;

import java.time.ZoneId;
import java.util.Map;

public interface  MiscellaneousCommandHandlers extends CommandHandlers {
    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Command
    @Usage("Show some basic information.")
    void info(HandlerCallback<Map<String, String>> callback);

    /**
     * Get all the time zone with index that has ZoneOffset's hour equals to the parameter.
     * @param offsetHour the time zone offset's hour
     * @return a map between a representative index with a ZoneId
     */
    @Command(value="list-timezones", additional = true)
    @Usage("Display all the time zone corresponding the the parameters.\nExample: `list-timezones 3` will list all" +
            " the time zone with the offset \"UTF+3\"")
    void listTimeZones(int offset_hour, HandlerCallback<Map<Integer, ZoneId>> callback);

    /**
     */
    @Command(value="set-timezone", additional = true)
    @Usage("Set the time zone by index. Use command `list-timezones` for the list of time zones with indexes.")
    void setTimeZone(int time_zone_id, HandlerCallback callback);

    @Override
    default Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor) {
        return ReflectionCommandHandlerGenerator.generate(
                MiscellaneousCommandHandlers.class, this, preprocessor
        );
    }
}
