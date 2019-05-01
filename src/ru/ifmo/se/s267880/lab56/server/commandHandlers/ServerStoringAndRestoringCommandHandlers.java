package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.csv.CsvReader;
import ru.ifmo.se.s267880.lab56.csv.CsvRowWithNamesWriter;
import ru.ifmo.se.s267880.lab56.server.Services;
import ru.ifmo.se.s267880.lab56.shared.BuildingLocation;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.communication.FileTransferRequest;
import ru.ifmo.se.s267880.lab56.shared.functional.ConsumerWithException;
import ru.ifmo.se.s267880.lab56.shared.functional.FunctionWithException;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.StoringAndRestoringCommandHandlers;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerStoringAndRestoringCommandHandlers extends ServerCommandHandlers
    implements StoringAndRestoringCommandHandlers
{
    public ServerStoringAndRestoringCommandHandlers(Services services) {
        super(services);
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
                getDataFrom(in).forEach(services.getUserState()::add);
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
            saveCollectionToFile(services.getUserState().getMeetingsCollection(), f);
            callback.onSuccess(new FileTransferRequest(name, f));
        } catch (IOException e) {
            callback.onError(new IOException("Cannot create temp file for exporting.", e));
        }
    }


    @Override
    public synchronized void open(String collectionName, HandlerCallback callback) {
        try {
            if (!services.getSqlHelper().getCollectionByName(collectionName).next()) {
                callback.onError(new Exception("There is no collection with name " + collectionName));
                return ;
            }
            services.getTokenVerifier().verify(
                    services.getUserState().getUserEmail(),
                    String.format("opening collection `%s`", collectionName),
                    60_000,
                    new HandlerCallback<>(tokenOk -> {
                        try {
                            if (tokenOk) {
                                services.getUserState().loadFromDatabase(collectionName);
                            }
                            callback.onSuccess(null);
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                    }, callback::onError)
            );
        } catch (SQLException e) {
            callback.onError(e);
        }
    }

    @Override
    public void save(HandlerCallback callback) {
        try {
            services.getUserState().storeToDatabase();
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void save(String name, HandlerCallback callback) {
        try {
            services.getUserState().storeToDatabase(name, true);
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
    @Deprecated
    public void loadFile(String path, HandlerCallback callback) {
        if (path != null) {
            try {
                services.getUserState().resetCollectionState(getDataFromFile(path));
                services.getUserState().updateStoringName(path);
            } catch (IOException | ParseException e)  { callback.onError(e); }
        }
        callback.onSuccess(null);
    }

    /**
     * Save all the collection into the file with name ....
     */
    @Override
    @Deprecated
    public synchronized void saveFile(HandlerCallback callback) {
        if (services.getUserState().getCollectionStoringName() == null) {
            callback.onError(new NullPointerException("Please use `save-file {String}` command to set the file name."));
            return;
        }
        saveFile(services.getUserState().getCollectionStoringName(), callback);
    }

    /**
     * Just change the current working file. The data of that file will be replaced.
     * @param path that path to the file.
     */
    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public void saveFile(String path, HandlerCallback callback) {
        try {
            saveCollectionToFile(services.getUserState().getMeetingsCollection(), new File(path));
            services.getUserState().resetCollectionState(services.getUserState().getMeetingsCollection());   // TODO make this line perform better since the collection is assigned to itself.
            services.getUserState().updateStoringName(path);
            callback.onSuccess(null);
        } catch (IOException e) {
            callback.onError(new IOException("Unable to write data into " + path, e));
        }
    }

    @Override
    public void listCollections(HandlerCallback<HashMap<String, String>> callback) {
        if (services.getUserState().getUserId() == -1) callback.onError(new Exception("You must login to server inorder to see the collections lists."));
        else try {
            HashMap<String, String> collectionsInfo = new HashMap<>();
            for (ResultSet rs = services.getUserState().getSqlHelper().getAllCollection(); rs.next(); ) {
                collectionsInfo.put(rs.getString("collection_name"), rs.getString("user_email"));
            }
            callback.onSuccess(collectionsInfo);
        } catch (SQLException e) {
            callback.onError(e);
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

}
