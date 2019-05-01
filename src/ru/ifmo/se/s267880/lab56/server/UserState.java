package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.Helper;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.ZoneUtils;
import ru.ifmo.se.s267880.lab56.shared.functional.FunctionWithException;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UserState {
    private int userId = -1;
    private List<Meeting> meetingsCollection = Collections.synchronizedList(new LinkedList<>());
    private List<Meeting> removedMeetings = Collections.synchronizedList(new LinkedList<>());
    private String collectionStoringName = null;
    private ZonedDateTime openSince = ZonedDateTime.now();
    private ZoneId timeZoneId = ZonedDateTime.now().getZone();
    private SQLHelper sqlHelper;

    public UserState(int userId, Connection connection) throws SQLException {
        this.userId = userId;
        this.sqlHelper = new SQLHelper(connection);
    }

    public UserState() {
        this.sqlHelper = null;
    }

    public synchronized void resetCollectionState(List<Meeting> newMeetings) {
        if (newMeetings == null) return;
        removedMeetings.clear();
        meetingsCollection.clear();
        meetingsCollection.addAll(newMeetings);
    }

    public synchronized void updateStoringName(String storingName) {
        if (!Objects.equals(storingName, collectionStoringName)) {
            collectionStoringName = storingName;
            openSince = ZonedDateTime.now();
        }
    }

    public synchronized void loadFromDatabase(String collectionName) throws Exception {
        if (sqlHelper == null) throw new NullPointerException("You are not signed in.");
        ResultSet res = sqlHelper.getCollectionByName(collectionName);
        if(!res.next()) {
            throw new Exception("Collection \"" + collectionName + "\" not found.");   // TODO (or not :p): create a class for this exception.
        }
        int collectionId = res.getInt("id");
        List<Meeting> meetings = sqlHelper.getMeetingListByCollectionId(collectionId, timeZoneId);
        resetCollectionState(meetings);
        updateStoringName(collectionName);
    }

    public synchronized void storeToDatabase() throws Exception {
        if (this.collectionStoringName == null) {
            throw new NullPointerException("Please use save <name> for save, because you did not save before.");
        }
        storeToDatabase(collectionStoringName, false);
    }

    public synchronized void storeToDatabase(String name, boolean toNewCollection) throws Exception {
        if (sqlHelper == null) throw new NullPointerException("You are not signed in.");
        ResultSet res = sqlHelper.getCollectionByName(name);
        if (!res.next()) {
            (res = sqlHelper.insertNewCollection(name, "asc-time", userId)).next();
        } else if (toNewCollection) {
            throw new InvalidParameterException("Collection with name \"" + name + "\" existed. Please choose another name.");
        }
        if (res.getInt("owner_id") != userId) {
            throw new Exception("You do not have the right to overwrite this collection. " +
                    "Use `save <name>` command instead to save this list as yours.");  // TODO create a separate exception
        }
        int collectionId = res.getInt("id");
        if (!toNewCollection) {
            sqlHelper.removeMeetings(removedMeetings);
        }
        List<Meeting> newCollections = meetingsCollection.stream()
                .map(FunctionWithException.toFunction(meeting -> !toNewCollection && meeting.getId().isPresent()
                        ? meeting
                        : sqlHelper.storeMeetingToDatabase(meeting, collectionId)
                ))
                .collect(Collectors.toList());
        resetCollectionState(newCollections);
        updateStoringName(name);
    }

    public synchronized Meeting transformMeetingTimeSameInstant(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameInstant(timeZoneId));
    }

    public synchronized Meeting transformMeetingTimeSameLocal(Meeting meeting) {
        return meeting.withTime(meeting.getTime().withZoneSameLocal(timeZoneId));
    }

    public synchronized void add(Meeting meeting) {
        meetingsCollection.add(transformMeetingTimeSameLocal(meeting));
    }

    public synchronized int findMeeting(Meeting meeting) {
        return meetingsCollection.indexOf(transformMeetingTimeSameLocal(meeting));
    }

    public synchronized void remove(int meetingNumber) {
        if (meetingNumber < 1 || meetingNumber > meetingsCollection.size()) {
            throw new IndexOutOfBoundsException("Number must be positive and not bigger than the number of meetings.");
        }
        Meeting m = meetingsCollection.remove(--meetingNumber);
        if (sqlHelper != null) {
            removedMeetings.add(m);
        }
    }

    public synchronized void clear() {
        if (sqlHelper != null) {
            removedMeetings.addAll(meetingsCollection);
        }
        meetingsCollection.clear();
    }

    public int getUserId() {
        return userId;
    }

    public String getUserEmail() throws SQLException {
        if (userId == -1) return null;
        ResultSet rs = sqlHelper.getUserById(userId);
        if (!rs.next()) return null;
        return rs.getString("email");
    }

    public List<Meeting> getMeetingsCollection() {
        meetingsCollection.sort(Comparator.comparing(Meeting::getTime));
        return Collections.unmodifiableList(meetingsCollection);
    }

    public String getCollectionStoringName() {
        return collectionStoringName;
    }

    public ZoneId getTimeZone() {
        return timeZoneId;
    }

    public SQLHelper getSqlHelper() {
        return sqlHelper;
    }

    public void setTimeZone(ZoneId timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public synchronized Map<String, String> generateInfo() throws SQLException {
        Map<String, String> result = new HashMap<>();
        result.put("user-email", getUserEmail());
        result.put("file", collectionStoringName);
        result.put("meeting-count", Integer.toString(meetingsCollection.size()));
        result.put("since", Helper.meetingDateFormat.format(openSince));
        result.put("time-zone", timeZoneId.toString() + " " + ZoneUtils.toUTCZoneOffsetString(timeZoneId));
        return result;
    }
}
