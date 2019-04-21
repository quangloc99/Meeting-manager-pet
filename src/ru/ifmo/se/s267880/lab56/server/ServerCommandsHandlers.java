package ru.ifmo.se.s267880.lab56.server;

import com.sun.istack.internal.NotNull;
import ru.ifmo.se.s267880.lab56.client.ClientInputPreprocessor;
import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Usage;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.Command;
import ru.ifmo.se.s267880.lab56.csv.CsvReader;
import ru.ifmo.se.s267880.lab56.csv.CsvRowWithNamesWriter;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;
import ru.ifmo.se.s267880.lab56.shared.functional.*;

/**
 * An implementation of CommandHandlersWithMeeting on server side.
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
    private String collectionStoringName;
    private ZonedDateTime fileOpenSince = ZonedDateTime.now();
    private ZoneId zoneId = ZonedDateTime.now().getZone();
    private PreparedStatements dataBaseQueryStatements;
    private List<Meeting> removedMeeting = Collections.synchronizedList(new LinkedList<>());

    public ServerCommandsHandlers(@NotNull List<Meeting> collection, Connection databaseConnection) throws SQLException {
        this.collection = collection;
        this.dataBaseQueryStatements = new PreparedStatements(databaseConnection);
    }

    private synchronized void updateStoringName(String path) {
        if ((path == null && collectionStoringName == null) || path.equals(collectionStoringName)) return;
        collectionStoringName = path;
        fileOpenSince = ZonedDateTime.now();
    }

    private Meeting transformMeetingTimeSameInstant(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameInstant(zoneId));
    }

    private Meeting transformMeetingTimeSameLocal(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameLocal(zoneId));
    }

    public synchronized String getCollectionStoringName() {
        return collectionStoringName;
    }

    /**
     * Add all data from another file into the current collection.
     * @param file
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doImport(File file, HandlerCallback callback) {
        try {
            try (InputStream in = new FileInputStream(file)) {
                collection.addAll(getDataFrom(in));
            }
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void export(String name, HandlerCallback<FileTransferRequest> callback) {
        try {
            File f = Helper.createTempFile();
            saveCollectionToFile(f);
            callback.onSuccess(new FileTransferRequest(name, f));
        } catch (IOException e) {
            callback.onError(new IOException("Cannot create temp file for exporting.", e));
        }
    }

    @Override
    public synchronized void open(String collectionName, HandlerCallback callback) {
        try {
            PreparedStatement st = dataBaseQueryStatements.getCollectionByName;
            st.setString(1, collectionName);
            ResultSet res = st.executeQuery();
            if (!res.next()) {
                callback.onError(new Exception("Collection \"" + collectionName + "\" not found."));   // TODO (or not :p): create a class for this exception.
                return ;
            }
            int collectionId = res.getInt("id");
            st = dataBaseQueryStatements.getMeetingsOfCollection;
            st.setInt(1, collectionId);
            List<Meeting> meetings = getDataFrom(st.executeQuery());
            collection.clear();
            collection.addAll(meetings);
            updateStoringName(collectionName);
            callback.onSuccess(null);
        } catch (SQLException e) {
            callback.onError(e);
        }
    }

    @Override
    public void save(HandlerCallback callback) {
        if (collectionStoringName == null) {
            callback.onError(new NullPointerException("Please use `save {String}` command to set the file name."));
            return;
        }
        save(collectionStoringName, false, callback);
    }

    @Override
    public void save(String name, HandlerCallback callback) { save(name, true, callback); }

    public void save(String name, boolean allNew, HandlerCallback callback) {
        try {
            dataBaseQueryStatements.getConnection().setAutoCommit(false);
            PreparedStatement st = dataBaseQueryStatements.getCollectionByName;
            st.setString(1, name);
            ResultSet res = st.executeQuery();
            if (!res.next()) {
                st = dataBaseQueryStatements.insertCollectionAndGetId;
                st.setString(1, name);
                st.setString(2, "asc-time");   // TODO add sort order
                res = st.executeQuery();
                res.next();
            }
            int collectionId = res.getInt("id");
            if (!allNew) {
                removedMeeting.forEach(ConsumerWithException.toConsumer(meeting -> {
                    PreparedStatement curSt = dataBaseQueryStatements.deleteMeeting;
                    if (!meeting.getId().isPresent()) return;
                    curSt.setInt(1, meeting.getId().getAsInt());
                    curSt.executeUpdate();
                }));
            }
            List<Meeting> newCollections = collection.stream()
                    .map(FunctionWithException.toFunction(
                            meeting -> !allNew && meeting.getId().isPresent() ? meeting : storeMeetingToDatabase(meeting, collectionId)
                    ))
                    .collect(Collectors.toList());
            removedMeeting.clear();
            collection.clear();
            collection.addAll(newCollections);
            dataBaseQueryStatements.getConnection().commit();
            updateStoringName(name);
            callback.onSuccess(null);
        } catch (SQLException e) {
            try {
                dataBaseQueryStatements.getConnection().rollback();
                callback.onError(e);
            } catch (SQLException e1) {
                callback.onError(e1);
            }
        } finally {
            try {
                dataBaseQueryStatements.getConnection().setAutoCommit(true);
            } catch(SQLException e) {
                callback.onError(e);
            }
        }
    }

    /**
     * Replace the current collection with the ones in another file. Also change the current working file to that file.
     * @param path the path to the file.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void loadFile(String path, HandlerCallback callback) {
        if (path != null) {
            try {
                List<Meeting> t = getDataFromFile(path);
                synchronized (collection) {
                    collection.clear();
                    collection.addAll(t);
                }
            } catch (IOException | ParseException e)  { callback.onError(e); }
        }
        updateStoringName(path);
        callback.onSuccess(null);
    }

    /**
     * Save all the collection into the file with name {@link #collectionStoringName}.
     */
    @Override
    public synchronized void saveFile(HandlerCallback callback) {
        if (collectionStoringName == null) {
            callback.onError(new NullPointerException("Please use `save-file {String}` command to set the file name."));
            return;
        }
        saveFile(collectionStoringName, callback);
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveFile(String path, HandlerCallback callback) {
        try {
            saveCollectionToFile(new File(path));
            updateStoringName(path);
        } catch (IOException e) {
            callback.onError(new IOException("Unable to write data into " + path, e));
        }
        callback.onSuccess(null);
    }

    private void saveCollectionToFile(File file) throws IOException {
        List<String> header = new LinkedList<>();
        header.add("meeting name");
        header.add("meeting time");
        header.add("duration");
        header.add("building number");
        header.add("floor number");
        try (CsvRowWithNamesWriter writer = new CsvRowWithNamesWriter(new FileOutputStream(file), header)) {
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
            }
        }
    }

    /**
     * Get the data from another file.
     * @param inputStream the stream of data to be transformed in to meetings.
     * @return the data of the file.
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

    private List<Meeting> getDataFrom(ResultSet rs) throws SQLException {
        List<Meeting> res = new LinkedList<>();
        while (rs.next()) {
            res.add(meetingFromResultSet(rs));
        }
        return res;
    }

    private Meeting meetingFromResultSet(ResultSet rs) throws SQLException {
        return new Meeting(
                rs.getInt("id"),
                rs.getString("name"),
                Duration.ofMinutes(rs.getInt("duration")),
                new BuildingLocation(rs.getInt("location_building"), rs.getInt("location_floor")),
                ZonedDateTime.ofInstant(rs.getTimestamp("time").toInstant(), zoneId)  // TODO make it display the right time on zoneId.
        );
    }

    private Meeting storeMeetingToDatabase(Meeting meeting, int collectionStoringId) throws SQLException {
        PreparedStatement ps = dataBaseQueryStatements.insertMeetingAndGetId;
        ps.setString(1, meeting.getName());
        ps.setLong(2, meeting.getDuration().toMinutes());
        ps.setInt(3, meeting.getLocation().getBuildingNumber());
        ps.setInt(4, meeting.getLocation().getFloor());
        ps.setTimestamp(5, Timestamp.from(meeting.getTime().toInstant()));
        ps.setInt(6, collectionStoringId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return meeting.withId(rs.getInt("id"));
    }

    /**
     * Get the data from another file.
     * @param path the path to the file.
     * @return the data of the file.
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
    @SuppressWarnings("unchecked")
    public void add(Meeting meeting, HandlerCallback callback) {
        collection.add(transformMeetingTimeSameLocal(meeting));
        callback.onSuccess(null);
    }

    /**
     * List all the meetings.
     * Note: Because the method will pass a list of object to the client so every meeting's must be transformed
     * to the current zone with same <b>instant</b>.
     */
    @Override
    public void show(HandlerCallback<List<Meeting>> callback) {
        callback.onSuccess(this.getCollection());
    }

    /**
     * Remove a meeting from the collection by value.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be removed.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove(Meeting meeting, HandlerCallback callback) {
        int num = collection.indexOf(transformMeetingTimeSameLocal(meeting));
        if (num == -1) callback.onSuccess(null);
        else remove(num, callback);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove(int num, HandlerCallback callback) {
        if (num < 1 || num > collection.size()) {
            callback.onError(new IndexOutOfBoundsException("removed id must be bigger than 0 and not bigger than the the collection size."));
            return ;
        }
        removedMeeting.add(collection.remove(num - 1));
        callback.onSuccess(null);
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting, HandlerCallback callback) {
        meeting = transformMeetingTimeSameLocal(meeting);
        synchronized (collection) {
            if (meeting.compareTo(Collections.min(collection)) >= 0) return;
            add(meeting, callback);
        }
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public synchronized void info(HandlerCallback<Map<String, String>> callback) {
        Map<String, String> result = new HashMap<>();
        result.put("file", collectionStoringName);
        result.put("meeting-count", Integer.toString(collection.size()));
        result.put("since", Helper.meetingDateFormat.format(fileOpenSince));
        result.put("time-zone", zoneId.toString() + " " + ZoneUtils.toUTCZoneOffsetString(zoneId));
        callback.onSuccess(result);
    }

    /**
     * Sort all the meeting ascending by their date.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void sortByDate(HandlerCallback callback) {
        Collections.sort(collection, Comparator.comparing(Meeting::getTime));
        callback.onSuccess(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sortBytime(HandlerCallback callback) {
        Collections.sort(collection, Comparator.comparing(Meeting::getDuration));
        callback.onSuccess(null);
    }

    /**
     * Reverse the order of the meetings.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void reverse(HandlerCallback callback) {
        Collections.reverse(collection);
        callback.onSuccess(null);
    }

    /**
     * Swap 2 meeting.
     * @param a the index of the first meeting.
     * @param b the index of the second meeting.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void swap(int a, int b, HandlerCallback callback) {
        synchronized (collection) {
            Collections.swap(collection, a - 1, b - 1);
        }
        callback.onSuccess(null);
    }

    /**
     * Clear the collection.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear(HandlerCallback callback) {
        removedMeeting.addAll(collection);
        collection.clear();
        callback.onSuccess(null);
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
    public void listTimeZones(int offsetHour, HandlerCallback<Map<Integer, ZoneId>> callback) {
        callback.onSuccess(ZoneUtils.getZonesBy(z -> ZoneUtils.toUTCZoneOffset(z).getTotalSeconds() / 3600 == offsetHour));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setTimeZone(int timeZoneKey, HandlerCallback callback) {
        if (!ZoneUtils.allZoneIds.containsKey(timeZoneKey)) {
            callback.onError(new NoSuchFieldException(String.format(
                    "There is no time zones with index %d. " +
                    "Please use command `list-time-zones` for the list of time zones", timeZoneKey)));
            return;
        }
        zoneId = ZoneUtils.allZoneIds.get(timeZoneKey);
        callback.onSuccess(null);
    }
}
