package ru.ifmo.se.s267880.lab56.server.services;

import ru.ifmo.se.s267880.lab56.server.UserState;
import ru.ifmo.se.s267880.lab56.server.commandHandlers.TokenVerifier;
import ru.ifmo.se.s267880.lab56.shared.EventEmitter;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class Services {
    private Socket clientSocket;
    private Sender messageToClientSender;
    private Broadcaster<MessageType> messageFromClientBroadcaster;
    private SQLHelper sqlHelper;
    private UserStatePool userStatePool;
    private EventEmitter<UserNotification> onNotificationEvent;
    private UserState userState;
    private TokenVerifier tokenVerifier;

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

        public Services build() {
            Services res = new Services();
            res.clientSocket = socket;
            res.messageToClientSender = Sender.fromSocket(socket);
            res.messageFromClientBroadcaster = new Broadcaster<>(Receiver.fromSocket(socket));
            res.userStatePool = userStatePool;
            res.onNotificationEvent = onNotificationEvent;
            res.sqlHelper = new SQLHelper(databaseConnection);
            res.userState = new UserState();
            res.tokenVerifier = new TokenVerifier(res.messageFromClientBroadcaster, res.messageToClientSender, mailSender);
            return res;
        }
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public void setUserStateWithId(int id) throws SQLException {
        if (id < 0) userState = new UserState();
        else userState = userStatePool.getUserState(id);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public Sender getMessageToClientSender() {
        return messageToClientSender;
    }

    public Broadcaster<MessageType> getMessageFromClientBroadcaster() {
        return messageFromClientBroadcaster;
    }

    public SQLHelper getSqlHelper() {
        return sqlHelper;
    }

    public EventEmitter<UserNotification> getOnNotificationEvent() {
        return onNotificationEvent;
    }

    public UserStatePool getUserStatePool() {
        return userStatePool;
    }

    public UserState getUserState() {
        return userState;
    }

    public TokenVerifier getTokenVerifier() {
        return tokenVerifier;
    }
}
