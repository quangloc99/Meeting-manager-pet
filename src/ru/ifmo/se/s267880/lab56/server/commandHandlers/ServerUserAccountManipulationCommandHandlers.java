package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.*;
import ru.ifmo.se.s267880.lab56.server.services.SQLHelper;
import ru.ifmo.se.s267880.lab56.server.services.Services;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.communication.UserNotification;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.UserAccountManipulationCommandHandlers;

import javax.mail.internet.InternetAddress;
import java.security.spec.InvalidKeySpecException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerUserAccountManipulationCommandHandlers extends ServerCommandHandlers
    implements UserAccountManipulationCommandHandlers
{

    public ServerUserAccountManipulationCommandHandlers(Services services) {
        super(services);
    }

    @Override
    public void register(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
        try {
            String userEmail = userEmailAndPassword.getKey().getAddress();
            if (services.getSqlHelper().getUserbyEmail(userEmail).next()) {
                callback.onError(new Exception("User with email " + userEmail + " has already existed."));
                return ;
            }
            if (userEmailAndPassword.getValue().length < 6) {
                callback.onError(new Exception("Password must contains at least 6 characters."));
                return ;
            }
            services.getTokenVerifier().verify(userEmail, "registration", 90_000, new HandlerCallback<>(tokenOk -> {
                if (tokenOk) try {
                    ResultSet rs = services.getSqlHelper().insertNewUser(userEmail, Crypto.hashPassword(userEmailAndPassword.getValue()));
                    rs.next();
                    services.setUserStateWithId(rs.getInt("id"));
                    services.getOnNotificationEvent().emit(new UserNotification(userEmail, "has joined"));
                } catch (SQLException | InvalidKeySpecException e) {
                    callback.onError(e);
                }
                callback.onSuccess(tokenOk);
            }, callback::onError));
        } catch (SQLException e) {
            callback.onError(e);
        }
    }

    @Override
    public void login(Map.Entry<InternetAddress, char[]> userEmailAndPassword, HandlerCallback<Boolean> callback) {
        try {
            String userEmail = userEmailAndPassword.getKey().getAddress();
            ResultSet rs = services.getSqlHelper().getUserbyEmail(userEmail);
            if (!rs.next()) {
                callback.onError(new Exception("Email or password not correct."));
                return ;
            }
            String passwordHash = rs.getString("password_hash");
            if (!Crypto.validatePassword(userEmailAndPassword.getValue(), passwordHash)) {
                callback.onError(new Exception("Email or password not correct."));
                return ;
            }
            services.setUserStateWithId(rs.getInt("id"));
            services.getOnNotificationEvent().emit(new UserNotification(userEmail, "has joined"));
            callback.onSuccess(true);
        } catch (SQLException | InvalidKeySpecException e) {
            callback.onError(e);
        }
    }

    @Override
    public void logout(HandlerCallback callback) {
        try {
            if (services.getUserState().getUserId() == -1) {
                callback.onError(new Exception("You are not login."));
                return ;
            }
            services.getOnNotificationEvent().emit(new UserNotification(
                    services.getUserState().getUserEmail(), "has left")
            );
            services.setUserStateWithId(-1);
            callback.onSuccess(null);
        } catch (SQLException e) {
            System.err.println("Error with Database.");
            callback.onError(e);
        }
    }

    @Override
    public void listUsers(HandlerCallback<String[]> callback) {
        if (services.getUserState().getUserId() == -1) callback.onError(new Exception("You must login to server inorder to see the user lists."));
        else try {
            SQLHelper sqlHelper = services.getSqlHelper();   // get the helper from the getUserState() because its from
            // the database containing with the user.
            List<String> res = new LinkedList<>();
            for (ResultSet rs = sqlHelper.getAllUser(); rs.next(); ) {
                res.add(rs.getString("email"));
            }
            callback.onSuccess(res.toArray(new String[0]));
        } catch (SQLException e) {
            callback.onError(e);
        }
    }
}
