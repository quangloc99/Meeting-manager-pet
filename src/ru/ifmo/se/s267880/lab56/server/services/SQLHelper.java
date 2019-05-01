package ru.ifmo.se.s267880.lab56.server.services;

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
    private Connection connection;

    public SQLHelper(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getUserByIdSt = null;
    public ResultSet getUserById(int id) throws SQLException {
        if (getUserByIdSt == null) {
            getUserByIdSt = connection.prepareStatement("select * from users where id = ?");
        }
        getUserByIdSt.setInt(1, id);
        return getUserByIdSt.executeQuery();
    }

    private PreparedStatement getUserByEmailSt = null;
    public ResultSet getUserbyEmail(@NotNull String email) throws SQLException {
        if (getUserByEmailSt == null) {
            getUserByEmailSt = connection.prepareStatement("select * from users where email = ?");
        }
        getUserByEmailSt.setString(1, email);
        return getUserByEmailSt.executeQuery();
    }

    private PreparedStatement getCollectionByNameSt = null;
    public ResultSet getCollectionByName(@NotNull  String name) throws SQLException {
        if (getCollectionByNameSt == null) {
            getCollectionByNameSt = connection.prepareStatement("select * from collections where name = ?");
        }
        getCollectionByNameSt.setString(1, name);
        return getCollectionByNameSt.executeQuery();
    }

    private PreparedStatement insertUserAndGetSt = null;
    public ResultSet insertNewUser(@NotNull String email, @NotNull String passwordHash) throws SQLException {
        if (insertUserAndGetSt == null) {
            insertUserAndGetSt = connection.prepareStatement("INSERT INTO users (email, password_hash) values (?, ?) RETURNING *");
        }
        insertUserAndGetSt.setString(1, email);
        insertUserAndGetSt.setString(2, passwordHash);
        return insertUserAndGetSt.executeQuery();
    }

    private PreparedStatement insertCollectionAndGetSt = null;
    public ResultSet insertNewCollection(@NotNull String name, String sortOrder, int ownerId) throws SQLException {
        if (insertCollectionAndGetSt == null) {
                insertCollectionAndGetSt = connection.prepareStatement(
                        "INSERT INTO collections (name, sort_order, owner_id) values (?, CAST(? AS meeting_collection_sort_order), ?) RETURNING *"
                );
        }
        insertCollectionAndGetSt.setString(1, name);
        insertCollectionAndGetSt.setString(2, sortOrder);   // TODO add sort order
        insertCollectionAndGetSt.setInt(3, ownerId);
        return insertCollectionAndGetSt.executeQuery();
    }

    private PreparedStatement getCollectionOfMeetingsSt = null;
    public List<Meeting> getMeetingListByCollectionId(int collectionId, ZoneId zoneId) throws SQLException {
        if (getCollectionOfMeetingsSt == null) {
            getCollectionOfMeetingsSt = connection.prepareStatement("select meetings.* from meetings where collection_id = ?");
        }
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

    private PreparedStatement deleteMeetingSt = null;
    public void removeMeetings(List<Meeting> meetings) throws SQLException {
        if (deleteMeetingSt == null) {
            deleteMeetingSt = connection.prepareStatement("DELETE FROM meetings WHERE id = ?");
        }
        meetings.forEach(ConsumerWithException.toConsumer(meeting -> {
            if (!meeting.getId().isPresent()) return;
            deleteMeetingSt.setInt(1, meeting.getId().getAsInt());
            deleteMeetingSt.executeUpdate();
        }));
    }

    private PreparedStatement insertMeetingAndGetSt = null;
    public Meeting storeMeetingToDatabase(Meeting meeting, int collectionStoringId) throws SQLException {
        if (insertMeetingAndGetSt == null) {
            insertMeetingAndGetSt = connection.prepareStatement(
                    "INSERT INTO meetings (name, duration, location_building, location_floor, time, collection_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?) " +
                            "RETURNING id"
            );
        }
        insertMeetingAndGetSt.setString(1, meeting.getName());
        insertMeetingAndGetSt.setLong(2, meeting.getDuration().toMinutes());
        insertMeetingAndGetSt.setInt(3, meeting.getLocation().getBuildingNumber());
        insertMeetingAndGetSt.setInt(4, meeting.getLocation().getFloor());
        insertMeetingAndGetSt.setTimestamp(5, Timestamp.from(meeting.getTime().toInstant()));
        insertMeetingAndGetSt.setInt(6, collectionStoringId);
        ResultSet rs = insertMeetingAndGetSt.executeQuery();
        rs.next();
        return meeting.withId(rs.getInt("id"));
    }

    private PreparedStatement getAllUsersPublicInfosSt = null;
    public ResultSet getAllUser() throws SQLException {
        if (getAllUsersPublicInfosSt == null) {
            getAllUsersPublicInfosSt = connection.prepareStatement("SELECT id, email FROM users");
        }
        return getAllUsersPublicInfosSt.executeQuery();
    }

    private PreparedStatement getAllCollectionsWithUsersSt = null;
    public ResultSet getAllCollection() throws SQLException {
        if (getAllCollectionsWithUsersSt == null) {
            getAllCollectionsWithUsersSt = connection.prepareStatement(
                    "SELECT collections.name AS collection_name, users.email As user_email " +
                            "from collections inner join users on collections.owner_id = users.id"
            );
        }
        return getAllCollectionsWithUsersSt.executeQuery();
    }

    public Connection getConnection() {
        return connection;
    }
}
