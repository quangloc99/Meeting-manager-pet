package ru.ifmo.se.s267880.lab56.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
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
    private static UserStatePool userStatePool;
    private static MailSender mailSender;
    public static void main(String[] args) {
        try (FileInputStream configFile = new FileInputStream("config.json")) {
            JsonElement elm = new JsonParser().parse(new InputStreamReader(configFile));
            mailSender = MailSender.fromJson(elm.getAsJsonObject().getAsJsonObject("mail"));
            try {
                JsonObject dbConfig = elm.getAsJsonObject().getAsJsonObject("database");
                databaseConnection = initDataBase(
                        dbConfig.get("url").getAsString(),
                        dbConfig.get("user").getAsString(),
                        dbConfig.get("password").getAsString()
                );
                userStatePool = new UserStatePool(databaseConnection);
            } catch (SQLException e) {
                System.err.println("Error while initialization data base: " + e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Problem while reading config.json: " + e.getMessage());
            System.err.println("If you have not added config.json file yet, please add it for program configuration.");
            System.exit(0);
        }

        QueryHandlerThread.Builder handlerThreadBuilder = new QueryHandlerThread.Builder();
        handlerThreadBuilder.setDatabaseConnection(databaseConnection);
        handlerThreadBuilder.setUserStatePool(userStatePool);
        handlerThreadBuilder.setOnNotificationEvent(onNotification);
        handlerThreadBuilder.setMailSender(mailSender);

        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            System.out.println("Server connected at " + ss.getLocalPort());
            while (true) {
                try {
                    handlerThreadBuilder.setSocket(ss.accept());
                    handlerThreadBuilder.build().start();
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

    private static Connection initDataBase(String url, String user, String password) throws SQLException, IOException {
        Connection res = DriverManager.getConnection(url, user, password);
        InputStream is = Class.class.getResourceAsStream("/ru/ifmo/se/s267880/lab56/server/res/create_tables.sql");
        if (is == null) throw new IOException("Cannot load script from resource.");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        Statement st = res.createStatement();
        st.executeUpdate(in.lines().collect(Collectors.joining("\n")));
        return res;
    }
}
