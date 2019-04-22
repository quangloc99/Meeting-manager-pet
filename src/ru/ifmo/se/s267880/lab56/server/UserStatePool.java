package ru.ifmo.se.s267880.lab56.server;

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
            pool.put(userId, new UserState(userId, connection) {
                @Override
                public void dispose() {
                    super.dispose();
                    if (referenceCount.get() > 0) return ;
                    pool.remove(getUserId());
                }
            });
        }
        UserState us = pool.get(userId);
        us.increaseReferenceCount();
        return us;
    }
}
