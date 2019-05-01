package ru.ifmo.se.s267880.lab56.server;

import ru.ifmo.se.s267880.lab56.server.commandHandlers.ServerCollectionManipulationCommandHandlers;
import ru.ifmo.se.s267880.lab56.server.commandHandlers.ServerMiscellaneousCommandHandlers;
import ru.ifmo.se.s267880.lab56.server.commandHandlers.ServerStoringAndRestoringCommandHandlers;
import ru.ifmo.se.s267880.lab56.server.commandHandlers.ServerUserAccountManipulationCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.*;
import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.ReflectionCommandHandlerGenerator;
import ru.ifmo.se.s267880.lab56.shared.communication.*;
import ru.ifmo.se.s267880.lab56.shared.functional.ConsumerWithException;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.CollectionManipulationCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.StoringAndRestoringCommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.UserAccountManipulationCommandHandlers;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class QueryHandlerThread extends Thread {
    private final Services services;

    public QueryHandlerThread(Services services) {
        this.services = services;
        CommandController commandController = new CommandController();
        services.getMessageFromClientBroadcaster().whenReceive(MessageType.REQUEST).listen(res -> {
            if (!(res instanceof CommandExecuteRequest)) return;
            CommandExecuteRequest qr = (CommandExecuteRequest)  res;
            commandController.execute(qr.getCommandName(), qr.getParameters(), new HandlerCallback<>(
                    o -> sendExecuteRespondToClient(generateResult(MessageType.RESPOND_SUCCESS, (Serializable) o)),
                    e -> {
                        if (e instanceof SQLException) {
                            e = new Exception("Error with database. If you see this message, please inform the creator.");
                        }
                        sendExecuteRespondToClient(generateResult(MessageType.RESPOND_FAIL, e));
                    }
            ));
        });
        services.getMessageFromClientBroadcaster().onError.listen(this::onDisconnectedToClient);
        Map<Class, CommandHandlers> handlersMap = new HashMap<>();
        handlersMap.put(CollectionManipulationCommandHandlers.class, new ServerCollectionManipulationCommandHandlers(services));
        handlersMap.put(StoringAndRestoringCommandHandlers.class, new ServerStoringAndRestoringCommandHandlers(services));
        handlersMap.put(UserAccountManipulationCommandHandlers.class, new ServerUserAccountManipulationCommandHandlers(services));
        handlersMap.put(MiscellaneousCommandHandlers.class, new ServerMiscellaneousCommandHandlers(services));

        handlersMap.forEach((cls, handlers) -> {
            ReflectionCommandHandlerGenerator.generate(cls, handlers, new ServerInputPreprocessor())
                    .forEach(commandController::addCommand);
            // also add additional handlers that not defined in the shard interfaces.
            ReflectionCommandHandlerGenerator.generate(handlers.getClass(), handlers, new ServerInputPreprocessor())
                    .forEach(commandController::addCommand);
        });

        services.getOnNotificationEvent().listen(this::notificationListener);
    }

    @Override
    public void run() {
        services.getMessageFromClientBroadcaster().run();
    }

    private void notificationListener(UserNotification notification) {
        try {
            services.getMessageToClientSender().sendWithStream(notification);
        } catch (IOException ignore) {}
    }

    private void sendExecuteRespondToClient(Respond res) {
        try {
            services.getMessageToClientSender().send(res);
        } catch (IOException e) {
            onDisconnectedToClient(e);
        }
    }

    private void onDisconnectedToClient(Exception e) {
        System.err.println(e.getMessage());
        System.out.printf("Disconnected to client %s.\n", services.getClientSocket().getInetAddress());
        services.getMessageFromClientBroadcaster().removeAllListeners();
        try {
            if (services.getUserState().getUserId() != -1) {
                services.getOnNotificationEvent().emit(new UserNotification(services.getUserState().getUserEmail(), "has left"));
            }
        } catch (SQLException e1) {
            System.err.println("Error with Database.");
            e1.printStackTrace();
        }
    }

    private Respond generateResult(MessageType respondType, Serializable result) {
        return new Respond(respondType, result, services.getUserState().getMeetingsCollection());
    }
}
