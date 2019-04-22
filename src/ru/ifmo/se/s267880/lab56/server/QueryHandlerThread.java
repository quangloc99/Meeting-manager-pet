package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.EventEmitter;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Meeting;
import ru.ifmo.se.s267880.lab56.shared.SharedCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class QueryHandlerThread extends Thread {
    private Socket client;
    private ServerCommandsHandlers commandsHandlers;
    private CommandController commandController;
    private Sender messageToClientSender;
    private Receiver messageFromClientReceiver;
    private EventEmitter<UserNotification> onNotificationEvent;
    private SQLHelper sqlHelper;
    private Consumer<UserNotification> notificationListener = notification -> {
        try { messageToClientSender.sendWithStream(notification); } catch (IOException ignore) {}
    };

    public QueryHandlerThread(Socket socket, Connection databaseConnection, UserStatePool userStatePool, EventEmitter<UserNotification> onNotificationEvent) throws SQLException {
        System.out.printf("Connected to client %s!\n", socket.getInetAddress());
        this.client = socket;
        messageToClientSender = Sender.fromSocket(socket);
        messageFromClientReceiver = Receiver.fromSocket(socket);
        sqlHelper = new SQLHelper(databaseConnection);

        commandsHandlers = new ServerCommandsHandlers() {
            @Override
            public void register(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
                try {
                    String userEmail = userEmailAndPassword.getKey().getAddress();
                    if (sqlHelper.getUserbyEmail(userEmail).next()) {
                        callback.onError(new Exception("User with email " + userEmail + " has already existed."));
                        return ;
                    }
                    // TODO validate with token
                    ResultSet rs = sqlHelper.insertNewUser(userEmail, Crypto.hashPassword(userEmailAndPassword.getValue()));
                    rs.next();
                    int userId = rs.getInt("id");
                    setState(userStatePool.getUserState(userId));
                    onNotificationEvent.emit(new UserNotification(userEmail, "joined"));
                    callback.onSuccess(true);
                } catch (SQLException | InvalidKeySpecException e) {
                    callback.onError(e);
                }
            }
        };
        commandController = new CommandController();
        ReflectionCommandHandlerGenerator.generate(SharedCommandHandlers.class, commandsHandlers, new ServerInputPreprocessor())
                .forEach(commandController::addCommand);

        this.onNotificationEvent = onNotificationEvent;
        onNotificationEvent.listen(notificationListener);
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
        onNotificationEvent.removeListener(notificationListener);
        try {
            onNotificationEvent.emit(new UserNotification(commandsHandlers.getState().getUserEmail(), "left"));
        } catch (SQLException e1) {
            System.err.println("Error with DataBase.");
            e1.printStackTrace();
        }
        commandsHandlers.getState().dispose();
    }

    private CommandExecuteRespond generateResult(CommandExecuteRespondStatus status, Serializable result) {
        LinkedList<Meeting> clonedCollection = new LinkedList<>(commandsHandlers.getState().getMeetingsCollection());
        clonedCollection.sort(Comparator.comparing(Meeting::getName));
        return new CommandExecuteRespond(status, result, clonedCollection);
    }
}
