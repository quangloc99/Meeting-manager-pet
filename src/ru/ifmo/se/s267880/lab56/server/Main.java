package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;

import java.io.*;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        String savedFileName = "untitled.csv";
        if (args.length > 0) {
            savedFileName = args[0];
        } else {
            System.out.println("No file name passed. Data will be read and saved into " + savedFileName);
        }

        ServerCommandsHandlers mm = null;
        try {
            mm = new ServerCommandsHandlers(Collections.synchronizedList(new LinkedList<Meeting>()));
            mm.open(savedFileName);
            mm.save();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }

        ServerCommandController cc = new ServerCommandController();
        ReflectionCommandAdder.addCommand(cc, CommandHandlersWithMeeting.class, mm, new ServerInputPreprocessor());

        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), cc).start();
                } catch (IOException e) {
                    System.err.println("Cannot run thread due to IOException: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Cannot open ServerSocket due to IOException " + e.getMessage());
            e.printStackTrace();
        }
    }
}
