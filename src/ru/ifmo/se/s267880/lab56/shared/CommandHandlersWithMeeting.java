package ru.ifmo.se.s267880.lab56.shared;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;


/**
 * This interface is used with {@link ReflectionCommandAdder} to add commands into {@link CommandController}.
 * Each methods with the annotation {@link Command} will be added into {@link CommandController}, and also each of them
 * also has {@link Usage} annotation, but it was not render in the document because it will be ugly if I do so.
 */
public interface CommandHandlersWithMeeting extends CommandHandlers {
    /**
     * Add all data from another file into the current collection.
     * @param path the path to the file.
     */
    @Command("import")
    @Usage("Add all data from the file given by the arg into the current collection.\nNote that the file name must be quoted")
    public void doImport(String path);

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Command
    @Usage("add new meeting into the collection.")
    public void add(Meeting meeting);

    /**
     * List all the meetings.
     */
    @Command
    @Usage("List all the meetings.")
    public void show();

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Command
    @Usage("remove the meeting correspond to the argument.")
    public void remove(Meeting meeting);

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Command
    @Usage("remove the meeting with index given by the argument.")
    public void remove(int num);

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Command("add_if_min")
    @Usage("add new meeting into the collection if it's date is before every other meeting in the collection.")
    public void addIfMin(Meeting meeting);

    /**
     * show file name, number of meeting and the time the file first open during this session.
     */
    @Command
    @Usage("Show some basic information.")
    public void info();

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Command(additional = true)
    @Usage("open a file with name given by arg. The content of the collection will be replaced.\n" +
            "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    public void open(String path) throws Exception;

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Command(value = "save-as", additional = true)
    @Usage("change the current working file.\n" +
            "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    public void saveAs(String path);

    /**
     * Sort all the meeting ascending by their date.
     */
    @Command(value="sort-by-date", additional = true)
    @Usage("sort all the meeting ascending by their date.")
    public void sortByDate();

    @Command(value="sort-by-duration", additional = true)
    @Usage("sort all the meetings ascending by their duration")
    public void sortBytime();

    /**
     * Reverse the order of the meetings.
     */
    @Command(additional = true)
    @Usage("reverse the order ot the meetings.")
    public void reverse();

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Command(additional = true)
    @Usage("swap 2 meetings with the given indexes")
    public void swap(int a, int b);

    /**
     * Clear the collection.
     */
    @Command(additional = true)
    @Usage("delete all the elements from the collection")
    public void clear();
}
