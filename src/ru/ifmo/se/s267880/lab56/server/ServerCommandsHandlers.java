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
import java.security.InvalidParameterException;
import java.sql.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;
import ru.ifmo.se.s267880.lab56.shared.functional.*;

import javax.mail.internet.InternetAddress;

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
abstract public class ServerCommandsHandlers implements SharedCommandHandlers {
    private UserState state;

    public ServerCommandsHandlers(UserState state) {
        this.state = state;
    }

    public ServerCommandsHandlers() {
        this.state = new UserState();
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
                getDataFrom(in).forEach(state::add);
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
            saveCollectionToFile(state.getMeetingsCollection(), f);
            callback.onSuccess(new FileTransferRequest(name, f));
        } catch (IOException e) {
            callback.onError(new IOException("Cannot create temp file for exporting.", e));
        }
    }

    @Override
    public synchronized void open(String collectionName, HandlerCallback callback) {
        try {
            state.loadFromDatabase(collectionName);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void save(HandlerCallback callback) {
        try {
            state.storeToDatabase();
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void save(String name, HandlerCallback callback) {
        try {
            state.storeToDatabase(name, true);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
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
                state.resetCollectionState(getDataFromFile(path));
                state.updateStoringName(path);
            } catch (IOException | ParseException e)  { callback.onError(e); }
        }
        callback.onSuccess(null);
    }

    /**
     * Save all the collection into the file with name ....
     */
    @Override
    public synchronized void saveFile(HandlerCallback callback) {
        if (state.getCollectionStoringName() == null) {
            callback.onError(new NullPointerException("Please use `save-file {String}` command to set the file name."));
            return;
        }
        saveFile(state.getCollectionStoringName(), callback);
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void saveFile(String path, HandlerCallback callback) {
        try {
            saveCollectionToFile(state.getMeetingsCollection(), new File(path));
            state.resetCollectionState(state.getMeetingsCollection());   // TODO make this line perform better since the collection is assigned to itself.
            state.updateStoringName(path);
            callback.onSuccess(null);
        } catch (IOException e) {
            callback.onError(new IOException("Unable to write data into " + path, e));
        }
    }

    private void saveCollectionToFile(List<Meeting> collection, File file) throws IOException {
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
        state.add(meeting);
        callback.onSuccess(null);
    }

    /**
     * List all the meetings.
     * Note: Because the method will pass a list of object to the client so every meeting's must be transformed
     * to the current zone with same <b>instant</b>.
     */
    @Override
    public void show(HandlerCallback<List<Meeting>> callback) {
        callback.onSuccess(state.getMeetingsCollection());
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
        int num = state.findMeeting(meeting);
        if (num == -1) callback.onSuccess(null);
        else state.remove(num);
    }

    /**
     * Remove a meeting from the collection by index.
     * @param num the index (base 1) of the element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void remove(int num, HandlerCallback callback) {
        try {
            state.remove(num);
            callback.onSuccess(null);
        } catch (IndexOutOfBoundsException e) {
            callback.onError(e);
        }
    }

    /**
     * Add new meeting into the collection if it's date is before every other meeting in the collection.
     * Note: Because the meeting is from the client so this method will transform meeting's time to have the same zone
     * on the server but with same <b>local</b>.
     * @param meeting the meeting wanted to be added.
     */
    @Override
    public void addIfMin(Meeting meeting, HandlerCallback callback) {
        meeting = state.transformMeetingTimeSameLocal(meeting);
        synchronized (state.getMeetingsCollection()) {
            if (meeting.compareTo(Collections.min(state.getMeetingsCollection())) < 0)
                state.add(meeting);
            callback.onSuccess(null);
        }
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public synchronized void info(HandlerCallback<Map<String, String>> callback) {
        try {
            callback.onSuccess(state.generateInfo());
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    /**
     * Clear the collection.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear(HandlerCallback callback) {
        state.clear();
        callback.onSuccess(null);
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
        state.setTimeZone(ZoneUtils.allZoneIds.get(timeZoneKey));
        callback.onSuccess(null);
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }
}
