package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.MeetingManagerInputPreprocessorJson;
import ru.ifmo.se.s267880.lab56.shared.BuildingLocation;
import ru.ifmo.se.s267880.lab56.shared.CommandHandlersWithMeeting;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.csv.CsvReader;
import ru.ifmo.se.s267880.lab56.csv.CsvRowWithNamesWriter;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static ru.ifmo.se.s267880.lab56.shared.Helper.uncheckedConsumer;
import static ru.ifmo.se.s267880.lab56.shared.Helper.uncheckedFunction;

/**
 * An implementation of CommandHandersWithMeeting on server side.
 * See {@link CommandHandlersWithMeeting} to know about which methods will be
 * used as commands.
 *
 * @author Tran Quang Loc
 * @see ReflectionCommandAdder
 * @see Command
 * @see Usage
 * @see CommandController
 * @see MeetingManagerInputPreprocessorJson
 */
public class ServerCommandsHandlers implements CommandHandlersWithMeeting {
    private List<Meeting> collection = null;
    private String currentFileName;
    private Date fileOpenSince;

    public ServerCommandsHandlers(List<Meeting> collection) {
        assert(collection != null);
        this.collection = collection;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * Add all data from another file into the current collection.
     * @param path the path to the file.
     */
    @Override
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
        return new CsvReader(new BufferedInputStream(new FileInputStream(path)), true)
            .getAllRowsWithNames().stream()
            .map(uncheckedFunction(row -> new Meeting(
                row.get("meeting name"),
                Duration.ofMinutes(Long.parseLong(row.get("duration"))),
                new BuildingLocation(
                    Integer.parseInt(row.get("building number")),
                    Integer.parseInt(row.get("floor number"))
                ),
                Helper.meetingDateFormat.parse(row.get("meeting time"))    // can throw ParseException
            )))
            .collect(Collectors.toList());
    }

    /**
     * Save all the collection into the file with name {@link #currentFileName}.
     */
    public synchronized void save() {
        List<String> header = new LinkedList<>();
        header.add("meeting name");
        header.add("meeting time");
        header.add("duration");
        header.add("building number");
        header.add("floor number");
        try {
            CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(
                new FileOutputStream(currentFileName),
                header
            );
            collection.stream()
                .map(meeting -> new HashMap<String, String>() {{    // Java 9 introduced Map.of, which might be more
                                                                    // comfortable to use, but helios (the ITMO server)
                                                                    // supports only java 8 for now.
                    put("meeting name", meeting.getName());
                    put("meeting time", Helper.meetingDateFormat.format(meeting.getTime()));
                    put("duration", Long.toString(meeting.getDuration().toMinutes()));
                    put("building number", Integer.toString(meeting.getLocation().getBuildingNumber()));
                    put("floor number", Integer.toString(meeting.getLocation().getFloor()));
                }})
                .forEachOrdered(uncheckedConsumer(writer::writeRow));
            writer.close();
        } catch (IOException e) {
            System.err.println("Unable to write data into file " + currentFileName);
            System.err.println(e);
        }
    }

//    /**
//     * Add meeting into the collection
//     */
//    @Command(additional = true)
//    @Usage("add new meeting into the collection with string a string instead of an object.\n" +
//           "The string can have this form: yyyy/MM/dd hh:mm:ss, <meeting-name>.\n" +
//           "the meeting name can still have commas, but it will be trim.")
//
//    public synchronized void add(String stringForm) throws ParseException {
//        stringForm = stringForm.trim();
//        String[] parts = stringForm.split(",");
//        add(new Meeting(
//                stringForm.substring(parts[0].length() + 2).trim(),
//                Helper.meetingDateFormat.parse(parts[0])
//        ));
//    }

    /**
     * Add meeting into the collection
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public synchronized void add(Meeting meeting) {
        collection.add(meeting);
    }

    /**
     * List all the meetings.
     */
    @Override
    public synchronized List<Meeting> show() {
        LinkedList<Meeting> clonedCollection = new LinkedList<>(collection);
        clonedCollection.sort((u, v) -> u.getName().compareTo(v.getName()));
        return clonedCollection;
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public synchronized void remove(Meeting meeting) {
        collection.remove(meeting);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public synchronized void remove(int num) {
        collection.remove(num - 1);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public synchronized void addIfMin(Meeting meeting) {
        if (meeting.compareTo(Collections.min(collection)) >= 0) return;
        add(meeting);
    }

    /**
     * show file name, number of meeting and the time the file first open during this session.
     */
    @Override
    public synchronized void info() {
        System.out.println("# Information");
        System.out.println("File name: " + currentFileName);
        System.out.println("Number of meeting: " + collection.size());
        System.out.println("File open since: " + Helper.meetingDateFormat.format(fileOpenSince));
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public synchronized void open(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File " + path + " not found. Creating new file.");
            file.createNewFile();
        }
        if (!file.isFile()) {
            throw new Exception(path + " must be a file.");
        }
        if (!file.canRead() || !file.canWrite()) {
            throw new Exception(path + " can not be read or write.");
        }
        collection.clear();
        try {
            collection.addAll(getDataFromFile(path));
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
    @Override
    public synchronized void saveAs(String path) {
        currentFileName = path;
        save();
        fileOpenSince = Calendar.getInstance().getTime();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    public synchronized void sortByDate() {
        Collections.sort(collection, (u, v) -> u.getTime().compareTo(v.getTime()));
    }

    @Override
    public synchronized void sortBytime() {
        Collections.sort(collection, (u, v) -> u.getDuration().compareTo(v.getDuration()));
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    public synchronized void reverse() {
        Collections.reverse(collection);
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    public synchronized void swap(int a, int b) {
        Collections.swap(collection, a - 1, b - 1);
    }

    /**
     * Clear the collection.
     */
    @Override
    public synchronized void clear() {
        collection.clear();
    }
}
