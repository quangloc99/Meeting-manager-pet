package ru.ifmo.se.s267880.lab5;

import JuniorAndCarlson.Meeting;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.Usage;
import ru.ifmo.se.s267880.lab5.commandControllerHelper.Command;
import ru.ifmo.se.s267880.lab5.csv.CsvReader;
import ru.ifmo.se.s267880.lab5.csv.CsvRowWithNamesWriter;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * A class that manage the meeting with basic operation: add, remove, ...
 *
 * This class also be used with {@link ru.ifmo.se.s267880.lab5.commandControllerHelper.ReflectionCommandAdder} to add commands into {@link CommandController}.
 * Each methods with the annotation {@link Command} will be added into {@link CommandController}, and also each of them
 * also has {@link Usage} annotation, but it was not render in the dog because it will be ugly if I do so.
 *
 * @author Tran Quang Loc
 * @see ru.ifmo.se.s267880.lab5.commandControllerHelper.ReflectionCommandAdder
 * @see Command
 * @see Usage
 * @see CommandController
 * @see MeetingManagerInputPreprocessor
 */
public class MeetingManager {
    private List<Meeting> collection = new LinkedList<>();
    private String currentFileName;
    private Date fileOpenSince;
    public MeetingManager(String path) {
        open(path);
        save();
    }

    /**
     * Add all data from another file into the current collection.
     * @param path the path to the file.
     */
    @Command("import")
    @Usage("Add all data from the file given by the arg into the current collection.\nNote that the file name must be quoted")
    public void doImport(String path) {
        try {
            collection.addAll(getDataFromFile(path));
        } catch (ParseException | IOException e) {
            System.err.println("File " + path + " does not exist or is corrupted. No data is imported");
        }
    }

    /**
     * Get the data from another file.
     * @param path the path to the file.
     * @return the data of the file.
     * @throws ParseException
     * @throws IOException
     */
    private List<Meeting> getDataFromFile(String path) throws ParseException, IOException {
        CsvReader reader = new CsvReader(new BufferedInputStream(new FileInputStream(path)), true);

        List<Meeting> newData = new LinkedList<>();
        while (true) {
            Map<String, String> row = reader.getNextRowWithNames();
            if (row == null) break;
            Meeting meeting = new Meeting(row.get("meeting name"), Helper.meetingDateFormat.parse(row.get("meeting time")));
            newData.add(meeting);
        }
        return newData;
    }

    /**
     * Save all the collection into the file with name {@link #currentFileName}.
     */
    public void save() {
        try {
            CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(
                    new FileOutputStream(currentFileName),
                    List.of("meeting name", "meeting time")
            );
            for (Meeting meeting: collection) {
                Map<String, String> row = new HashMap<>();
                row.put("meeting time", Helper.meetingDateFormat.format(meeting.getTime()));
                row.put("meeting name", meeting.getName());
                writer.writeRow(row);
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Unable to write data into file " + currentFileName);
            System.err.println(e);
        }
    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Command
    @Usage("Add new meeting into the collection.")
    public void add(Meeting meeting) {
        collection.add(meeting);
    }

    /**
     * List all the meetings.
     */
    @Command
    @Usage("List all the meetings.")
    public void show() {
        System.out.println("# Meeting list:");
        int i = 1;
        for (Meeting meeting: collection) {
            System.out.printf("%d) %s\n", i++, meeting);
        }
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Command
    @Usage("If arg is an object then the correspond meeting in the collection will be removed.")
    public void remove(Meeting meeting) {
        collection.remove(meeting);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Command
    @Usage("If arg is a number then the meeting with the given index (1-base indexed) will be removed.")
    public void remove(int num) {
        collection.remove(num - 1);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Command("add_if_min")
    @Usage("Add new meeting into the collection if it's date is before every other meeting in the collection.")
    public void addIfMin(Meeting meeting) {
        if (meeting.compareTo(Collections.min(collection)) >= 0) return;
        add(meeting);
    }

    /**
     * Show file name, number of meeting and the time the file first open during this session.
     */
    @Command
    @Usage("Show some basic information.")
    public void info() {
        System.out.println("# Information");
        System.out.println("File name: " + currentFileName);
        System.out.println("Number of meeting: " + collection.size());
        System.out.println("File open since: " + Helper.meetingDateFormat.format(fileOpenSince));
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Command
    @Usage("[Additional] Open a file with name given by arg. The content of the collection will be replaced.\n" +
           "Note that the file name must be quoted" )
    public void open(String path) {
        collection.clear();
        try {
            collection = getDataFromFile(path);
        } catch (ParseException | IOException e) {
            System.err.println(e);
            System.err.println("The collection is initialized empty and still be saved into " + path);
        }
        fileOpenSince = Calendar.getInstance().getTime();
        currentFileName = path;
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Command("save-as")
    @Usage("[Additional] Change the current working file.\nNote that the file name must be quoted.")
    public void saveAs(String path) {
        currentFileName = path;
        save();
        fileOpenSince = Calendar.getInstance().getTime();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Command
    @Usage("[Additional] sort all the meeting ascending by their date.")
    public void sort() {
        Collections.sort(collection);
    }

    /**
     * Reverse the order of the meetings.
     */
    @Command
    @Usage("[Additional] Reverse the order ot the meetings.")
    public void reverse() {
        Collections.reverse(collection);
    }
}
