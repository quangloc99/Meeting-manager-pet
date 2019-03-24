package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;

import java.io.*;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static volatile boolean hasResult = false;

    public static void main(String[] args) {
        String savedFileName = "untitled.csv";
//        if (args.length > 0) {
//            savedFileName = args[0];
//        } else {
//            System.out.println("No file name passed. Data will be read and saved into " + savedFileName);
//        }
//        System.out.println("Use \"help\" to display the help message. Use \"list-commands\" to display all the commands.");

        ServerCommandsHandlers mm = null;
        try {
            mm = new ServerCommandsHandlers(Collections.synchronizedList(new LinkedList<Meeting>()));
            mm.open(savedFileName);
            mm.save();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }

        ServerQueryCommandController cc = new ServerQueryCommandController();
        ReflectionCommandAdder.addCommand(
                cc, CommandHandlersWithMeeting.class,
                mm,
                new ServerInputPreprocessor()
        );

        Lock commandHandlingLock = new ReentrantLock();
        Condition commandExecuted = commandHandlingLock.newCondition();
        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT);) {
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), cc).start();
                } catch (IOException e) {
                    System.err.println("Cannot run thread due to IOException: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
