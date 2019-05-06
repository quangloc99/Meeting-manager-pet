package ru.ifmo.se.s267880.lab56.server.services;

import ru.ifmo.se.s267880.lab56.server.UserState;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserStatePool {
    private Map<Integer, UserState> pool = new HashMap<>();
    private Connection connection;
    private long expiryTime = 3600L;    // 1 hour
    private long cleanUpPeriod = 900L;  // 15 minutes

    public UserStatePool(Connection connection) {
        this.connection = connection;
    }
    public UserStatePool(Connection connection, long expiryTime, long cleanUpPeriod) {
        this.connection = connection;
        this.expiryTime = expiryTime;
        this.cleanUpPeriod = cleanUpPeriod;
    }

    public synchronized UserState getUserState(int userId) throws SQLException {
        cleanUp();
        if (!pool.containsKey(userId)) {
            pool.put(userId, new UserState(userId, connection));
        }
        UserState us = pool.get(userId);
        return us;
    }

    private volatile boolean cleaning = false;
    private void cleanUp() {
        if (cleaning) return;
        cleaning = true;
        new Thread(() -> {
            try { Thread.sleep(cleanUpPeriod); } catch (InterruptedException ignore) {}
            cleaning = false;
        }).start();
        pool.entrySet().removeIf(it -> ZonedDateTime.now().isAfter(it.getValue().getLastModifyingTime().plusSeconds(expiryTime)));
    }
}
