package ru.ifmo.se.s267880.lab56.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatements {
    public final PreparedStatement getCollectionByName;
    public final PreparedStatement getMeetingsOfCollection;
    public final PreparedStatement insertCollectionAndGetId;
    public final PreparedStatement insertMeetingAndGetId;
    public final PreparedStatement deleteMeeting;

    private Connection connection;

    public PreparedStatements(Connection connection) throws SQLException {
        this.connection = connection;
        getCollectionByName = connection.prepareStatement("select id, sort_order from collections where name = ?");
        getMeetingsOfCollection = connection.prepareStatement("select meetings.* from meetings where collection_id = ?");
        insertCollectionAndGetId = connection.prepareStatement(
                "INSERT INTO collections (name, sort_order) values (?, CAST(? AS meeting_collection_sort_order)) RETURNING id"
        );
        insertMeetingAndGetId = connection.prepareStatement(
                "INSERT INTO meetings (name, duration, location_building, location_floor, time, collection_id) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "RETURNING id"
        );
        deleteMeeting = connection.prepareStatement("DELETE FROM meetings WHERE id = ?");
    }

    public Connection getConnection() {
        return connection;
    }
}
