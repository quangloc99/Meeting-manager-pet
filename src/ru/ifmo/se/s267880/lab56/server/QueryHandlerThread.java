package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
                    o -> sendExecuteRespondToClient(generateResult(MessageType.RESPOND_SUCCESS, (Serializable) o)),
                    e -> sendExecuteRespondToClient(generateResult(MessageType.RESPOND_FAIL, e))
            ));
        });
        this.messageFromClientBroadcaster.onError.listen(this::onDisconnectedToClient);

        this.commandsHandlers = this.createCommandHandler();
        this.commandController = new CommandController();
        ReflectionCommandHandlerGenerator.generate(SharedCommandHandlers.class, commandsHandlers, new ServerInputPreprocessor())
                .forEach(commandController::addCommand);

        onNotificationEvent.listen(notificationListener);
    }

    private void sendExecuteRespondToClient(Respond res) {
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

    private Respond generateResult(MessageType respondType, Serializable result) {
        LinkedList<Meeting> clonedCollection = new LinkedList<>(commandsHandlers.getState().getMeetingsCollection());
        clonedCollection.sort(Comparator.comparing(Meeting::getName));
        return new Respond(respondType, result, clonedCollection);
    }

    private void listenForToken(String token, long timeOut, HandlerCallback<Boolean> callback)  {
        long currentMillis = System.currentTimeMillis();
        Consumer<Message<MessageType>> onRecevingToken = new Consumer<Message<MessageType>>() {
            @Override
            public void accept(Message<MessageType> msg) {
                if (System.currentTimeMillis() - currentMillis > timeOut) {
                    callback.onError(new TimeoutException("Your token has been expired."));
                    messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(this);
                    return ;
                }
                if (!(msg instanceof Respond)) return;
                String res = ((Respond) msg).getResult();
                String parts[] = res.split(":");
                if (parts.length != 2 || !parts[0].equals("Token")) return;
                if (parts[1].equals(token)) {
                    callback.onSuccess(true);
                    messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(this);
                }
                else if (parts[1].equals("\\abort")) {
                    callback.onSuccess(false);
                    messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(this);
                } else try {
                    messageToClientSender.send(new TokenRequest("Your token is incorrect. Enter it again."));
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        };
        messageFromClientBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).listen(onRecevingToken);
    }

    private ServerCommandsHandlers createCommandHandler() {
        InputStream mailTemplateFile = Main.class.getResourceAsStream("res/email-template.html");
        String mailTemplate = new BufferedReader(new InputStreamReader(mailTemplateFile)).lines().collect(Collectors.joining("\n"));

        return new ServerCommandsHandlers() {
            @Override
            public void register(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
                try {
                    String userEmail = userEmailAndPassword.getKey().getAddress();
                    if (sqlHelper.getUserbyEmail(userEmail).next()) {
                        callback.onError(new Exception("User with email " + userEmail + " has already existed."));
                        return ;
                    }
                    String token = Helper.generateToken();
                    String mail = MessageFormat.format(mailTemplate, "registration", token);
                    mailSender.sendHTMLMail(userEmail, "Token for registration", mail);

                    listenForToken(token, 90_000, new HandlerCallback<>(tokenOk -> {
                        if (tokenOk) try {
                            ResultSet rs = sqlHelper.insertNewUser(userEmail, Crypto.hashPassword(userEmailAndPassword.getValue()));
                            rs.next();
                            setState(userStatePool.getUserState(rs.getInt("id")));
                            onNotificationEvent.emit(new UserNotification(userEmail, "has joined"));
                        } catch (SQLException | InvalidKeySpecException e) {
                            callback.onError(e);
                        }
                        callback.onSuccess(tokenOk);
                    }, callback::onError));

                    messageToClientSender.send(new TokenRequest(
                            "A token for registration has been send to your mail box. " +
                                    "Enter it to complete your registration. " +
                                    "Token will be expired in 90 seconds."
                    ));
                } catch (IOException | SQLException e) {
                    callback.onError(e);
                } catch (MessagingException e) {
                    callback.onError(new Exception("Token cannot be sent."));
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
