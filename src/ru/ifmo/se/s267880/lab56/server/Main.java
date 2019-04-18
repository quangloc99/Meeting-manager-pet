package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandAdder;
import ru.ifmo.se.s267880.lab56.shared.communication.ResultToClient;
import ru.ifmo.se.s267880.lab56.shared.communication.ResultToClientStatus;

import java.io.*;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        String savedFileName = null;
        if (args.length > 0) {
            savedFileName = args[0];
        }

        ServerCommandsHandlers mm = new ServerCommandsHandlers(Collections.synchronizedList(new LinkedList<Meeting>()));
        try {
            mm.load(savedFileName);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }

        ServerCommandController cc = new ServerCommandController();
        ReflectionCommandAdder.addCommand(cc, SharedCommandHandlers.class, mm, new ServerInputPreprocessor());


        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            System.out.println("Server connected at " + ss.getLocalPort());
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), cc) {
                        @Override
                        ResultToClient generateResult(ResultToClientStatus status, Serializable result) {
                            LinkedList<Meeting> clonedCollection = new LinkedList<>(mm.getCollection());
                            clonedCollection.sort(Comparator.comparing(Meeting::getName));
                            return new ResultToClient(status, result, clonedCollection);
                        }
                    }.start();
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
