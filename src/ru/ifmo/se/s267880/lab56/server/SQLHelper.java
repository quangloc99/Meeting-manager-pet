package ru.ifmo.se.s267880.lab56.server;

import com.sun.istack.internal.NotNull;
import ru.ifmo.se.s267880.lab56.shared.BuildingLocation;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.functional.ConsumerWithException;

import java.sql.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

public class SQLHelper {
    private final PreparedStatement getUserByEmailSt;
    private final PreparedStatement getUserByIdSt;
    private final PreparedStatement getCollectionByNameSt;
    private final PreparedStatement getCollectionOfMeetingsSt;
    private final PreparedStatement insertUserAndGetIdSt;
    private final PreparedStatement insertCollectionAndGetIdSt;
    private final PreparedStatement insertMeetingAndGetIdSt;
    private final PreparedStatement deleteMeetingSt;

    private Connection connection;

    public SQLHelper(Connection connection) throws SQLException {
        this.connection = connection;
        getUserByIdSt = connection.prepareStatement("select * from users where id = ?");
        getUserByEmailSt = connection.prepareStatement("select * from users where email = ?");
        getCollectionByNameSt = connection.prepareStatement("select id, sort_order from collections where name = ?");
        getCollectionOfMeetingsSt = connection.prepareStatement("select meetings.* from meetings where collection_id = ?");
        insertCollectionAndGetIdSt = connection.prepareStatement(
                "INSERT INTO collections (name, sort_order) values (?, CAST(? AS meeting_collection_sort_order)) RETURNING id"
        );
        insertMeetingAndGetIdSt = connection.prepareStatement(
                "INSERT INTO meetings (name, duration, location_building, location_floor, time, collection_id) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "RETURNING id"
        );
        insertUserAndGetIdSt = connection.prepareStatement("INSERT INTO users (email, password_hash) values (?, ?) RETURNING id");
        deleteMeetingSt = connection.prepareStatement("DELETE FROM meetings WHERE id = ?");
    }

    public ResultSet getUserById(int id) throws SQLException {
        getUserByIdSt.setInt(1, id);
        return getUserByIdSt.executeQuery();
    }

    public ResultSet getUserbyEmail(@NotNull String email) throws SQLException {
        getUserByEmailSt.setString(1, email);
        return getUserByEmailSt.executeQuery();
    }

    public ResultSet getCollectionByName(@NotNull  String name) throws SQLException {
        getCollectionByNameSt.setString(1, name);
        return getCollectionByNameSt.executeQuery();
    }

    public ResultSet insertNewUser(@NotNull String email, @NotNull String passwordHash) throws SQLException {
        insertUserAndGetIdSt.setString(1, email);
        insertUserAndGetIdSt.setString(2, passwordHash);
        return insertUserAndGetIdSt.executeQuery();
    }

    public ResultSet insertNewCollection(@NotNull String name, String sortOrder) throws SQLException {
        insertCollectionAndGetIdSt.setString(1, name);
        insertCollectionAndGetIdSt.setString(2, sortOrder);   // TODO add sort order
        return insertCollectionAndGetIdSt.executeQuery();
    }

    public List<Meeting> getMeetingListByCollectionId(int collectionId, ZoneId zoneId) throws SQLException {
        getCollectionOfMeetingsSt.setInt(1, collectionId);
        ResultSet rs = getCollectionOfMeetingsSt.executeQuery();
        List<Meeting> res = new LinkedList<>();
        while (rs.next()) {
            res.add(new Meeting(
                    rs.getInt("id"),
                    rs.getString("name"),
                    Duration.ofMinutes(rs.getInt("duration")),
                    new BuildingLocation(rs.getInt("location_building"), rs.getInt("location_floor")),
                    ZonedDateTime.ofInstant(rs.getTimestamp("time").toInstant(), zoneId)  // TODO make it display the right time on zoneId.
            ));
        }
        return res;
    }

    public void removeMeetings(List<Meeting> meetings) throws SQLException {
        meetings.forEach(ConsumerWithException.toConsumer(meeting -> {
            if (!meeting.getId().isPresent()) return;
            deleteMeetingSt.setInt(1, meeting.getId().getAsInt());
            deleteMeetingSt.executeUpdate();
        }));
    }

    public Meeting storeMeetingToDatabase(Meeting meeting, int collectionStoringId) throws SQLException {
        insertMeetingAndGetIdSt.setString(1, meeting.getName());
        insertMeetingAndGetIdSt.setLong(2, meeting.getDuration().toMinutes());
        insertMeetingAndGetIdSt.setInt(3, meeting.getLocation().getBuildingNumber());
        insertMeetingAndGetIdSt.setInt(4, meeting.getLocation().getFloor());
        insertMeetingAndGetIdSt.setTimestamp(5, Timestamp.from(meeting.getTime().toInstant()));
        insertMeetingAndGetIdSt.setInt(6, collectionStoringId);
        ResultSet rs = insertMeetingAndGetIdSt.executeQuery();
        rs.next();
        return meeting.withId(rs.getInt("id"));
    }


    public Connection getConnection() {
        return connection;
    }
}
