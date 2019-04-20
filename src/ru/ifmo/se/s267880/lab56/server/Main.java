package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.communication.UserNotification;

import java.io.*;
import java.net.ServerSocket;

public class Main {
    private static EventEmitter<UserNotification> onNotification = new EventEmitter<>();
    public static void main(String[] args) {
        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            System.out.println("Server connected at " + ss.getLocalPort());
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), onNotification).start();
                } catch (IOException e) {
                    System.err.println("Cannot run thread due to IOException: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot load ServerSocket due to IOException " + e.getMessage());
            e.printStackTrace();
        }
    }
}
