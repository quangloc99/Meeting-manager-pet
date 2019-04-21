package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.communication.UserNotification;

import java.io.*;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class Main {
    private static EventEmitter<UserNotification> onNotification = new EventEmitter<>();
    private static Connection databaseConnection;
    public static void main(String[] args) {
        try {
            databaseConnection = initDataBase();
        } catch (SQLException e) {
            System.err.println("Error while initialization data base: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            System.out.println("Server connected at " + ss.getLocalPort());
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), databaseConnection, onNotification).start();
                } catch (IOException | SQLException e) {
                    System.err.println("Cannot run thread: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot load ServerSocket due to IOException " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Connection initDataBase() throws SQLException, IOException {
        Connection res = DriverManager.getConnection(
            ServerConfig.DB_URL, ServerConfig.DB_USER, ServerConfig.DB_PASSWORD
        );
        InputStream is = Class.class.getResourceAsStream("/ru/ifmo/se/s267880/lab56/server/res/create_tables.sql");
        if (is == null) throw new IOException("Cannot load script from resource.");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        Statement st = res.createStatement();
        st.executeUpdate(in.lines().collect(Collectors.joining("\n")));
        return res;
    }
}
