package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.client.ClientInputPreprocessor;
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
 * @see ClientInputPreprocessor
 */
public class ServerCommandsHandlers implements CommandHandlersWithMeeting {
    private List<Meeting> collection = null;
    private String currentFileName;
    private Date fileOpenSince = Calendar.getInstance().getTime();

    public ServerCommandsHandlers(List<Meeting> collection) {
        assert(collection != null);
        this.collection = collection;
    }

    void updateFileName(String path) {
        if ((path == null && currentFileName == null) || path.equals(currentFileName)) return;
        currentFileName = path;
        fileOpenSince = Calendar.getInstance().getTime();
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * Add all data from another file into the current collection.
     * @param inputStream
     */
    @Override
    public void doImport(InputStream inputStream) throws ParseException, IOException {
        collection.addAll(getDataFrom(inputStream));
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public synchronized void load(String path) throws Exception {
        if (path != null) {
            List<Meeting> t = getDataFromFile(path);
            collection.clear();
            collection.addAll(t);
        }
        updateFileName(path);
    }

    /**
     * Save all the collection into the file with name {@link #currentFileName}.
     */
    @Override
    public synchronized void save() throws Exception {
        if (currentFileName == null) {
            throw new NullPointerException("Please use `save-as {String}` command to set the file name.");
        }
        saveAs(currentFileName);
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Override
    public synchronized void saveAs(String path) throws IOException {
        List<String> header = new LinkedList<>();
        header.add("meeting name");
        header.add("meeting time");
        header.add("duration");
        header.add("building number");
        header.add("floor number");
        try (CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(new FileOutputStream(path), header)) {
            collection.stream()
                .map(meeting -> new HashMap<String, String>() {{
                    // Java 9 introduced Map.of, which might be more
                    // comfortable to use, but helios (the ITMO server)
                    // supports only java 8 for now.
                    put("meeting name", meeting.getName());
                    put("meeting time", Helper.meetingDateFormat.format(meeting.getTime()));
                    put("duration", Long.toString(meeting.getDuration().toMinutes()));
                    put("building number", Integer.toString(meeting.getLocation().getBuildingNumber()));
                    put("floor number", Integer.toString(meeting.getLocation().getFloor()));
                }})
                .forEachOrdered(uncheckedConsumer(writer::writeRow));
                updateFileName(path);
        } catch (IOException e) {
            throw new IOException("Unable to write data into " + path, e);
        }
    }

    /**
     * Get the data from another file.
     * @param inputStream the stream of data to be transformed in to meetings.
     * @return the data of the file.
     * @throws ParseException
     * @throws IOException
     */
    private List<Meeting> getDataFrom(InputStream inputStream) throws ParseException, IOException {
        return new CsvReader(inputStream, true)
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
     * Get the data from another file.
     * @param path the path to the file.
     * @return the data of the file.
     * @throws ParseException
     * @throws IOException
     */
    private List<Meeting> getDataFromFile(String path) throws ParseException, IOException {
        return getDataFrom(new BufferedInputStream(new FileInputStream(path)));
    }

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
    public List<Meeting> show() {
        synchronized (collection) {
            return Collections.unmodifiableList(collection);
        }
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
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public synchronized Map<String, String> info() {
        Map<String, String> result = new HashMap<>();
        result.put("file", currentFileName);
        result.put("meeting-count", Integer.toString(collection.size()));
        result.put("since", Helper.meetingDateFormat.format(fileOpenSince));
        return result;
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

    public List<Meeting> getCollection() {
        synchronized (collection) {
            return Collections.unmodifiableList(collection);
        }
    }
}
