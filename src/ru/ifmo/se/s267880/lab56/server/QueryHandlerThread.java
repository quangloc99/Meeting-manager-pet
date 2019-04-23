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
    private Broadcaster<MessageType> messageFromClientBroadcaster;
    private EventEmitter<UserNotification> onNotificationEvent;
    private SQLHelper sqlHelper;
    private UserStatePool userStatePool;
    private MailSender mailSender;

    private Consumer<UserNotification> notificationListener = notification -> {
        try { messageToClientSender.sendWithStream(notification); } catch (IOException ignore) {}
    };

    public static class Builder {
        private Socket socket;
        private EventEmitter<UserNotification> onNotificationEvent;
        private Connection databaseConnection;
        private UserStatePool userStatePool;
        private MailSender mailSender;

        public void setDatabaseConnection(Connection databaseConnection) { this.databaseConnection = databaseConnection; }
        public void setOnNotificationEvent(EventEmitter<UserNotification> onNotificationEvent) { this.onNotificationEvent = onNotificationEvent; }
        public void setSocket(Socket socket) { this.socket = socket; }
        public void setUserStatePool(UserStatePool userStatePool) { this.userStatePool = userStatePool; }
        public void setMailSender(MailSender mailSender) { this.mailSender = mailSender; }

        public QueryHandlerThread build() throws SQLException {
            QueryHandlerThread res = new QueryHandlerThread();
            res.client = socket;
            res.onNotificationEvent = onNotificationEvent;
            res.userStatePool = userStatePool;
            res.sqlHelper = new SQLHelper(databaseConnection);
            res.mailSender = mailSender;
            res.init();
            return res;
        }
    }

    private QueryHandlerThread() {}

    private void init() {
        System.out.printf("Connected to client %s!\n", client.getInetAddress());
        this.messageToClientSender = Sender.fromSocket(client);
        this.messageFromClientBroadcaster = new Broadcaster<>(Receiver.fromSocket(client));
        new Thread(this.messageFromClientBroadcaster).start();

        this.messageFromClientBroadcaster.whenReceive(MessageType.REQUEST).listen(res -> {
            if (!(res instanceof CommandExecuteRequest)) return;
            CommandExecuteRequest qr = (CommandExecuteRequest)  res;
            commandController.execute(qr.getCommandName(), qr.getParameters(), new HandlerCallback<>(
                    o -> sendExecuteRespondToClient(generateResult(CommandExecuteRespondStatus.SUCCESS, (Serializable) o)),
                    e -> sendExecuteRespondToClient(generateResult(CommandExecuteRespondStatus.FAIL, e))
            ));
        });
        this.messageFromClientBroadcaster.onError.listen(this::onDisconnectedToClient);

        this.commandsHandlers = this.createCommandHandler();
        this.commandController = new CommandController();
        ReflectionCommandHandlerGenerator.generate(SharedCommandHandlers.class, commandsHandlers, new ServerInputPreprocessor())
                .forEach(commandController::addCommand);

        onNotificationEvent.listen(notificationListener);
    }

    private void sendExecuteRespondToClient(CommandExecuteRespond res) {
        try {
            messageToClientSender.send(res);
        } catch (IOException e) {
//            onDisconnectedToClient(new CommunicationIOException("Cannot send data to client.", e));
            // no need to call the above method, because when there is an error with socket, the broadCaster will
            // broad cast it.
            // TODO handle this part less tricky
        }
    }

    private void onDisconnectedToClient(Exception e) {
        System.err.println(e.getMessage());
        System.out.printf("Disconnected to client %s.\n", client.getInetAddress());
        onNotificationEvent.removeListener(notificationListener);
        messageFromClientBroadcaster.removeAllListeners();
        try {
            if (commandsHandlers.getState().getUserId() != -1) {
                onNotificationEvent.emit(new UserNotification(commandsHandlers.getState().getUserEmail(), "has left"));
            }
        } catch (SQLException e1) {
            System.err.println("Error with Database.");
            e1.printStackTrace();
        }
    }

    private CommandExecuteRespond generateResult(CommandExecuteRespondStatus status, Serializable result) {
        LinkedList<Meeting> clonedCollection = new LinkedList<>(commandsHandlers.getState().getMeetingsCollection());
        clonedCollection.sort(Comparator.comparing(Meeting::getName));
        return new CommandExecuteRespond(status, result, clonedCollection);
    }

    private ServerCommandsHandlers createCommandHandler() {
        return new ServerCommandsHandlers() {
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
                    setState(userStatePool.getUserState(rs.getInt("id")));
                    onNotificationEvent.emit(new UserNotification(userEmail, "has joined"));
                    callback.onSuccess(true);
                } catch (SQLException | InvalidKeySpecException e) {
                    callback.onError(e);
                }
            }

            @Override
            public void login(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
                try {
                    String userEmail = userEmailAndPassword.getKey().getAddress();
                    ResultSet rs = sqlHelper.getUserbyEmail(userEmail);
                    if (!rs.next()) {
                        callback.onError(new Exception("Email or password not correct."));
                        return ;
                    }
                    String passwordHash = rs.getString("password_hash");
                    if (!Crypto.validatePassword(userEmailAndPassword.getValue(), passwordHash)) {
                        callback.onError(new Exception("Email or password not correct."));
                        return ;
                    }
                    setState(userStatePool.getUserState(rs.getInt("id")));
                    onNotificationEvent.emit(new UserNotification(userEmail, "has joined"));
                    callback.onSuccess(true);
                } catch (SQLException | InvalidKeySpecException e) {
                    callback.onError(e);
                }
            }

            @Override
            public void logout(HandlerCallback callback) {
                try {
                    if (getState().getUserId() == -1) {
                        callback.onError(new Exception("You are not login."));
                        return ;
                    }
                    onNotificationEvent.emit(new UserNotification(getState().getUserEmail(), "has left"));
                    setState(new UserState());
                    callback.onSuccess(null);
                } catch (SQLException e) {
                    System.err.println("Error with Database.");
                    callback.onError(e);
                }
            }

        };
    }
}
