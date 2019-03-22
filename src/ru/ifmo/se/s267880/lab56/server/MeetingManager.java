package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.MeetingManagerInputPreprocessorJson;
import ru.ifmo.se.s267880.lab56.shared.BuildingLocation;
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
import java.util.stream.IntStream;

import static ru.ifmo.se.s267880.lab56.shared.Helper.uncheckedConsumer;
import static ru.ifmo.se.s267880.lab56.shared.Helper.uncheckedFunction;

/**
 * A class that manage the meeting with basic operation: add, remove, ...
 *
 * This class also be used with {@link ReflectionCommandAdder} to add commands into {@link CommandController}.
 * Each methods with the annotation {@link Command} will be added into {@link CommandController}, and also each of them
 * also has {@link Usage} annotation, but it was not render in the dog because it will be ugly if I do so.
 *
 * @author Tran Quang Loc
 * @see ReflectionCommandAdder
 * @see Command
 * @see Usage
 * @see CommandController
 * @see MeetingManagerInputPreprocessorJson
 */
public class MeetingManager {
    private List<Meeting> collection = null;
    private String currentFileName;
    private Date fileOpenSince;

    public MeetingManager(List<Meeting> collection) {
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
    @Command
    @Usage("add new meeting into the collection.")
    public synchronized void add(Meeting meeting) {
        collection.add(meeting);
    }

    /**
     * List all the meetings.
     */
    @Command
    @Usage("List all the meetings.")
    public synchronized void show() {
        System.out.println("# Meeting list:");
        Iterator<Integer> counter = IntStream.rangeClosed(1, collection.size()).iterator();
        collection.stream()
                .map(meeting -> String.format("%3d) %s", counter.next(), meeting))
                .forEachOrdered(System.out::println);
    }

    /**
     * Remove a meeting from the collection by value.
     * @param meeting the meeting wanted to be removed.
     */
    @Command
    @Usage("remove the meeting correspond to the argument.")
    public synchronized void remove(Meeting meeting) {
        collection.remove(meeting);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Command
    @Usage("remove the meeting with index given by the argument.")
    public synchronized void remove(int num) {
        collection.remove(num - 1);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * @param meeting the meeting wanted to be added.
     */
    @Command("add_if_min")
    @Usage("add new meeting into the collection if it's date is before every other meeting in the collection.")
    public synchronized void addIfMin(Meeting meeting) {
        if (meeting.compareTo(Collections.min(collection)) >= 0) return;
        add(meeting);
    }

    /**
     * show file name, number of meeting and the time the file first open during this session.
     */
    @Command
    @Usage("Show some basic information.")
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
    @Command(additional = true)
    @Usage("open a file with name given by arg. The content of the collection will be replaced.\n" +
           "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
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
    @Command(value = "save-as", additional = true)
    @Usage("change the current working file.\n" +
           "Note that if the file name contains special characters (e.g \".\", \",\", \" \", \"\\\", ...), then it must be quoted." )
    public synchronized void saveAs(String path) {
        currentFileName = path;
        save();
        fileOpenSince = Calendar.getInstance().getTime();
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Command(value="sort-by-date", additional = true)
    @Usage("sort all the meeting ascending by their date.")
    public synchronized void sortByDate() {
        Collections.sort(collection, (u, v) -> u.getTime().compareTo(v.getTime()));
    }

    @Command(value="sort-by-duration", additional = true)
    @Usage("sort all the meetings ascending by their duration")
    public synchronized void sortBytime() {
        Collections.sort(collection, (u, v) -> u.getDuration().compareTo(v.getDuration()));
    }

    /**
     * Reverse the order of the meetings.
     */
    @Command(additional = true)
    @Usage("reverse the order ot the meetings.")
    public synchronized void reverse() {
        Collections.reverse(collection);
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Command(additional = true)
    @Usage("swap 2 meetings with the given indexes")
    public synchronized void swap(int a, int b) {
        Collections.swap(collection, a - 1, b - 1);
    }

    /**
     * Clear the collection.
     */
    @Command(additional = true)
    @Usage("delete all the elements from the collection")
    public synchronized void clear() {
        collection.clear();
    }
}
