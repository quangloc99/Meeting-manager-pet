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
    private List<Meeting> meetingsCollection = Collections.synchronizedList(new LinkedList<>());
    private List<Meeting> removedMeetings = Collections.synchronizedList(new LinkedList<>());
    private String collectionStoringName = null;
    private ZonedDateTime openSince = ZonedDateTime.now();
    private ZoneId timeZoneId = ZonedDateTime.now().getZone();
    private SQLHelper sqlHelper;

    public UserState(Connection connection) throws SQLException {
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

    public synchronized void storeToDatabase() throws SQLException, InvalidParameterException {
        if (this.collectionStoringName == null) {
            throw new NullPointerException("Please use save <name> for save, because you did not save before.");
        }
        storeToDatabase(collectionStoringName, false);
    }

    public synchronized void storeToDatabase(String name, boolean toNewCollection) throws SQLException, InvalidParameterException {
        if (sqlHelper == null) throw new NullPointerException("You are not signed in.");
        ResultSet res = sqlHelper.getCollectionByName(name);
        if (!res.next()) {
            (res = sqlHelper.insertNewCollection(name, "asc-time")).next();
        } else if (toNewCollection) {
            throw new InvalidParameterException("Collection with name \"" + name + "\" existed. Please choose another name.");
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
        removedMeetings.add(meetingsCollection.remove(--meetingNumber));
    }

    public synchronized void clear() {
        removedMeetings.addAll(meetingsCollection);
        meetingsCollection.clear();
    }

    public List<Meeting> getMeetingsCollection() {
        return Collections.unmodifiableList(meetingsCollection);
    }

    public List<Meeting> getRemovedMeetings() {
        return Collections.unmodifiableList(removedMeetings);
    }

    public String getCollectionStoringName() {
        return collectionStoringName;
    }

    public ZoneId getTimeZone() {
        return timeZoneId;
    }

    public void setTimeZone(ZoneId timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public synchronized Map<String, String> generateInfo() {
        Map<String, String> result = new HashMap<>();
        result.put("file", collectionStoringName);
        result.put("meeting-count", Integer.toString(meetingsCollection.size()));
        result.put("since", Helper.meetingDateFormat.format(openSince));
        result.put("time-zone", timeZoneId.toString() + " " + ZoneUtils.toUTCZoneOffsetString(timeZoneId));
        return result;
    }
}
