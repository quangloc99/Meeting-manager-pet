package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRespond;
import ru.ifmo.se.s267880.lab56.shared.communication.CommandExecuteRespondStatus;

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
        mm.load(savedFileName, HandlerCallback.ofErrorHandler(e -> {
            System.err.println(e.getMessage());
            System.exit(0);
        }));

        CommandController cc = new CommandController();
        ReflectionCommandHandlerGenerator.generate(SharedCommandHandlers.class, mm, new ServerInputPreprocessor())
                .forEach(cc::addCommand);

        try (ServerSocket ss = new ServerSocket(Config.COMMAND_EXECUTION_PORT)) {
            System.out.println("Server connected at " + ss.getLocalPort());
            while (true) {
                try {
                    new QueryHandlerThread(ss.accept(), cc) {
                        @Override
                        CommandExecuteRespond generateResult(CommandExecuteRespondStatus status, Serializable result) {
                            LinkedList<Meeting> clonedCollection = new LinkedList<>(mm.getCollection());
                            clonedCollection.sort(Comparator.comparing(Meeting::getName));
                            return new CommandExecuteRespond(status, result, clonedCollection);
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
