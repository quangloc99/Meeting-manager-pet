package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.client.ClientInputPreprocessor;
import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.csv.CsvReader;
import ru.ifmo.se.s267880.lab56.csv.CsvRowWithNamesWriter;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.se.s267880.lab56.shared.functional.*;

/**
 * An implementation of CommandHandersWithMeeting on server side.
 * See {@link SharedCommandHandlers} to know about which methods will be
 * used as commands.
 *
 * @author Tran Quang Loc
 * @see ReflectionCommandHandlerGenerator
 * @see Command
 * @see Usage
 * @see CommandController
 * @see ClientInputPreprocessor
 */
public class ServerCommandsHandlers implements SharedCommandHandlers {
    private List<Meeting> collection = null;
    private String currentFileName;
    private ZonedDateTime fileOpenSince = ZonedDateTime.now();
    private ZoneId zoneId = ZonedDateTime.now().getZone();

    public ServerCommandsHandlers(List<Meeting> collection) {
        assert(collection != null);
        this.collection = collection;
    }

    private synchronized void updateFileName(String path) {
        if ((path == null && currentFileName == null) || path.equals(currentFileName)) return;
        currentFileName = path;
        fileOpenSince = ZonedDateTime.now();
    }

    private Meeting transformMeetingTimeSameInstant(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameInstant(zoneId));
    }

    private Meeting transformMeetingTimeSameLocal(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameLocal(zoneId));
    }

    public synchronized String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * Add all data from another file into the current collection.
     * @param file
     */
    @Override
    public void doImport(File file) throws ParseException, IOException {
        try (InputStream in = new FileInputStream(file)) {
            collection.addAll(getDataFrom(in));
        }
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    public void load(String path) throws Exception {
        if (path != null) {
            List<Meeting> t = getDataFromFile(path);
            synchronized (collection) {
                collection.clear();
                collection.addAll(t);
            }
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
    public void saveAs(String path) throws IOException {
        List<String> header = new LinkedList<>();
        header.add("meeting name");
        header.add("meeting time");
        header.add("duration");
        header.add("building number");
        header.add("floor number");
        try (CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(new FileOutputStream(path), header)) {
            synchronized (collection) {
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
                    .forEachOrdered(ConsumerWithException.toConsumer(writer::writeRow));
                updateFileName(path);
            }
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
                .map(FunctionWithException.toFunction(row -> new Meeting(
                        row.get("meeting name"),
                        Duration.ofMinutes(Long.parseLong(row.get("duration"))),
                        new BuildingLocation(
                                Integer.parseInt(row.get("building number")),
                                Integer.parseInt(row.get("floor number"))
                        ),
                        ZonedDateTime.parse(row.get("meeting time"), Helper.meetingDateFormat)    // can throw ParseException
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
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be add.
     */
    @Override
    public void add(Meeting meeting) {
        collection.add(transformMeetingTimeSameLocal(meeting));
    }

    /**
     * List all the meetings.
     * Note: Because the method will pass a list of object to the client so every meeting's must be transformed
     * to the current zone with same <b>instant</b>.
     */
    @Override
    public List<Meeting> show() {
        return this.getCollection();
    }

    /**
     * Remove a meeting from the collection by value.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    public void remove(Meeting meeting) {
        collection.remove(transformMeetingTimeSameLocal(meeting));
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    public void remove(int num) {
        collection.remove(num - 1);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting) {
        meeting = transformMeetingTimeSameLocal(meeting);
        synchronized (collection) {
            if (meeting.compareTo(Collections.min(collection)) >= 0) return;
            add(meeting);
        }
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
        result.put("time-zone", zoneId.toString() + " " + ZoneUtils.toUTCZoneOffsetString(zoneId));
        return result;
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    public void sortByDate() {
        Collections.sort(collection, Comparator.comparing(Meeting::getTime));
    }

    @Override
    public void sortBytime() {
        Collections.sort(collection, Comparator.comparing(Meeting::getDuration));
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    public void reverse() {
        Collections.reverse(collection);
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    public void swap(int a, int b) {
        synchronized (collection) {
            Collections.swap(collection, a - 1, b - 1);
        }
    }

    /**
     * Clear the collection.
     */
    @Override
    public void clear() {
        collection.clear();
    }

    /**
     * List all the meetings.
     * Note: The method will transform every meeting's time to the current zone with same <b>instant</b>.
     */
    public List<Meeting> getCollection() {
        synchronized (collection) {
            return collection.stream()
                    .map(this::transformMeetingTimeSameInstant)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Map<Integer, ZoneId> listTimeZones(int offsetHour) {
        return ZoneUtils.getZonesBy(z -> ZoneUtils.toUTCZoneOffset(z).getTotalSeconds() / 3600 == offsetHour);
    }

    @Override
    public void setTimeZone(int timeZoneKey) throws Exception {
        if (!ZoneUtils.allZoneIds.containsKey(timeZoneKey)) {
            throw new NoSuchFieldException(String.format(
                    "There is no time zones with index %d. " +
                    "Please use command `list-time-zones` for the list of time zones", timeZoneKey));
        }
        zoneId = ZoneUtils.allZoneIds.get(timeZoneKey);
    }
}
