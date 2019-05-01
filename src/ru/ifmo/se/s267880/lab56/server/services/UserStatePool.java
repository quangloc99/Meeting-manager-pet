package ru.ifmo.se.s267880.lab56.server.services;

import ru.ifmo.se.s267880.lab56.server.UserState;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserStatePool {
    private Map<Integer, UserState> pool = new HashMap<>();
    private Connection connection;

    public UserStatePool(Connection connection) {
        this.connection = connection;
    }

    public UserState getUserState(int userId) throws SQLException {
        if (!pool.containsKey(userId)) {
            pool.put(userId, new UserState(userId, connection));
        }
        UserState us = pool.get(userId);
        return us;
    }
}
