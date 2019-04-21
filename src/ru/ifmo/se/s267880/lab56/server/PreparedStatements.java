package ru.ifmo.se.s267880.lab56.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatements {
    public final PreparedStatement getCollectionByName;
    public final PreparedStatement getMeetingsOfCollection;

    private Connection connection;

    public PreparedStatements(Connection connection) throws SQLException {
        this.connection = connection;
        getCollectionByName = connection.prepareStatement("select id, sort_order from collections where name = ?");
        getMeetingsOfCollection = connection.prepareStatement("select meetings.* from meetings where collection_id = ?");
    }

    public Connection getConnection() {
        return connection;
    }
}
