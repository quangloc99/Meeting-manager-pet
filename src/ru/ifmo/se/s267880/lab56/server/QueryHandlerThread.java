package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.SharedCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Consumer;

public class QueryHandlerThread extends Thread {
    private Socket client;
    private ServerCommandsHandlers commandsHandlers;
    private CommandController commandController;
    private Sender messageToClientSender;
    private Receiver messageFromClientReceiver;

    public Socket getClient() {
        return client;
    }

    public QueryHandlerThread(Socket socket) {
        System.out.printf("Connected to client %s!\n", socket.getInetAddress());
        this.client = socket;
        messageToClientSender = Sender.fromSocket(socket);
        messageFromClientReceiver = Receiver.fromSocket(socket);

        commandsHandlers = new ServerCommandsHandlers(Collections.synchronizedList(new LinkedList<>()));
        commandController = new CommandController();
        ReflectionCommandHandlerGenerator.generate(SharedCommandHandlers.class, commandsHandlers, new ServerInputPreprocessor())
                .forEach(commandController::addCommand);
    }

    @Override
    public void run() {
        try {
            CommandExecuteRequest qr = messageFromClientReceiver.receiveWithStream();
            commandController.execute(qr.getCommandName(), qr.getParameters(), new HandlerCallback<>(
                    this::onCommandSuccessfulExecuted,
                    this::onErrorWhenExecutingCommand
            ));
        } catch (IOException | ClassNotFoundException e) {
            onDisconnectedToClient(new CommunicationIOException("Cannot read data sent from client.", e));
        }
    }

    private void onCommandSuccessfulExecuted(Object o) {
        sendDataToClient(generateResult(CommandExecuteRespondStatus.SUCCESS, (Serializable) o), this, this::onDisconnectedToClient);
    }

    private void onErrorWhenExecutingCommand(Exception e) {
        sendDataToClient(generateResult(CommandExecuteRespondStatus.FAIL, e), this, this::onDisconnectedToClient);
    }

    private void sendDataToClient(CommandExecuteRespond res, Runnable onSuccess, Consumer<Exception> onError) {
        try {
            messageToClientSender.send(res);
            new Thread(onSuccess).start();
        } catch (IOException e) {
            onError.accept(new CommunicationIOException("Cannot send data to client.", e));
        }
    }

    private void onDisconnectedToClient(Exception e) {
        System.err.println(e.getMessage());
        System.out.printf("Disconnected to client %s.\n", client.getInetAddress());
    }

    private CommandExecuteRespond generateResult(CommandExecuteRespondStatus status, Serializable result) {
        LinkedList<Meeting> clonedCollection = new LinkedList<>(commandsHandlers.getCollection());
        clonedCollection.sort(Comparator.comparing(Meeting::getName));
        return new CommandExecuteRespond(status, result, clonedCollection);
    }
}
