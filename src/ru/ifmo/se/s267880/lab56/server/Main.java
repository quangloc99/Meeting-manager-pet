package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.Config;
import ru.ifmo.se.s267880.lab56.shared.QueryToServer;

import javax.management.Query;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Config.PORT);
            while (true) {
                new QueryHandler(ss.accept()).start();
            }
//            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
