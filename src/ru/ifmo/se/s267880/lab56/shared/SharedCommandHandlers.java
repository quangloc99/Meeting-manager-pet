package ru.ifmo.se.s267880.lab56.shared;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;

import java.io.File;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;


/**
 * This interface is used with {@link ReflectionCommandHandlerGenerator} to add commands into {@link CommandController}.
 * Each methods with the annotation {@link Command} will be added into {@link CommandController}, and also each of them
 * also has {@link Usage} annotation, but it was not render in the document because it will be ugly if I do so.
 */
public interface SharedCommandHandlers extends CommandHandlers {
    /**
     * Add all data from another file into the current collection.
     * @param file the file that the data will be imported from.
     */
    @Command("import")
    @Usage(
            value = "Add all data from the file given by the arg into the current collection.\nNote that the file name must be quoted",
            params = {"String"}
    )
    void doImport(File file, HandlerCallback callback);

    @Command
    @Usage(
            value = "Export/download the current collection and save it with csv format."
    )
    void export(String name, HandlerCallback<FileTransferRequest> callback);

    // TODO: add more detailed usage.
    @Command
    @Usage("Open a collection with the given name.")
    void open(String collectionName, HandlerCallback callback);
    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
//    @Command
    @Usage("load a file with name given by arg. The content of the collection will be replaced.\n" +
            "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    void loadFile(String path, HandlerCallback callback);

//    @Command
    @Usage("save current collection. Note that initially there is no file name yet, so please use `save-as {String}` first.")
    void saveFile(HandlerCallback callback) ;

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
//    @Command
    @Usage(
       "change the current working file.\n" +
       "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted.\n" +
       "After executing this command, the current file name changed."
    )
    void saveFile(String path, HandlerCallback callback);

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Command
    @Usage(value = "add new meeting into the collection.", params = {"MeetingJson"})
    void add(Meeting meeting, HandlerCallback callback);

    /**
     * List all the meetings.
     */
    @Command
    @Usage("List all the meetings.")
    void show(HandlerCallback<List<Meeting>> callback);

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Command
    @Usage(value = "remove the meeting correspond to the argument.", params = {"MeetingJson"})
    void remove(Meeting meeting, HandlerCallback callback);

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Command
    @Usage("remove the meeting with index given by the argument.")
    void remove(int num, HandlerCallback callback);

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Command("add_if_min")
    @Usage(value = "add new meeting into the collection if it's date is before every other meeting in the collection.",
            params = {"MeetingJson"})
    void addIfMin(Meeting meeting, HandlerCallback callback);

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Command
    @Usage("Show some basic information.")
    void info(HandlerCallback<Map<String, String>> callback);


    /**
     * Sort all the meeting ascending by their date.
     */
    @Command(value="sort-by-date", additional = true)
    @Usage("sort all the meeting ascending by their date.")
    void sortByDate(HandlerCallback callabck);

    @Command(value="sort-by-duration", additional = true)
    @Usage("sort all the meetings ascending by their duration")
    void sortBytime(HandlerCallback callback);

    /**
     * Reverse the order of the meetings.
     */
    @Command(additional = true)
    @Usage("reverse the order ot the meetings.")
    void reverse(HandlerCallback callback);

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Command(additional = true)
    @Usage("swap 2 meetings with the given indexes")
    void swap(int a, int b, HandlerCallback callback);

    /**
     * Clear the collection.
     */
    @Command(additional = true)
    @Usage("delete all the elements from the collection")
    void clear(HandlerCallback callback);

    /**
     * Get all the time zone with index that has ZoneOffset's hour equals to the parameter.
     * @param offsetHour the time zone offset's hour
     * @return a map between a representative index with a ZoneId
     */
    @Command(value="list-time-zones", additional = true)
    @Usage("Display all the time zone corresponding the the parameters.\nExample: `list-time-zones 3` will list all" +
            " the time zone with the offset \"UTF+3\"")
    void listTimeZones(int offsetHour, HandlerCallback<Map<Integer, ZoneId>> callback);

    /**
     * Set the current time zone corresponding to to the key in {@link ZoneUtils#allZoneIds}
     * @param timeZoneKey the time zone's key in {@link ZoneUtils#allZoneIds}
     */
    @Command(value="set-time-zone", additional = true)
    @Usage("Set the time zone by index. Use command `list-time-zones` for the list of time zones with indexes.")
    void setTimeZone(int timeZoneKey, HandlerCallback callback);
}
