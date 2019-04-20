package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

abstract public class QueryHandlerThread extends Thread {
    private Socket client;
    private CommandController cc;

    public Socket getClient() {
        return client;
    }

    public QueryHandlerThread(Socket socket, CommandController cc) {
        System.out.printf("Connected to client %s!\n", socket.getInetAddress());
        this.client = socket;
        this.cc = cc;
    }

    @Override
    public void run() {
        try {
            CommandExecuteRequest qr = Message.receive(Receiver.fromSocket(client));
            cc.execute(qr.getCommandName(), qr.getParameters(), new HandlerCallback<>(this::onCommandSuccessfulExecuted, this::onErrorWhenExecutingCommand));
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
            res.send(Sender.fromSocket(client));
            new Thread(onSuccess).start();
        } catch (IOException e) {
            onError.accept(new CommunicationIOException("Cannot send data to client.", e));
        }
    }

    private void onDisconnectedToClient(Exception e) {
        System.err.println(e.getMessage());
        System.out.printf("Disconnected to client %s.\n", client.getInetAddress());
    }

    abstract CommandExecuteRespond generateResult(CommandExecuteRespondStatus status, Serializable result);
}
