package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.io.IOException;
import java.util.function.Consumer;

public class CommandToServerExecutor {
    private Broadcaster<MessageType> messageFromServerBroadcaster;
    private Sender messageToServerSender;
    private HandlerCallback<Respond> callback;
    private Consumer<Exception> errorHandler;
    private Consumer<Message<MessageType>> respondFailHandler;
    private Consumer<Message<MessageType>> respondSuccessHandler;

    public CommandToServerExecutor(Broadcaster<MessageType> messageFromServerBroadcaster, Sender messageToServerSender) {
        this.messageFromServerBroadcaster = messageFromServerBroadcaster;
        this.messageToServerSender = messageToServerSender;
        errorHandler = messageFromServerBroadcaster.onError.listen(e -> {
            removeHandlers();
            callback.onError(new CommunicationIOException());
        });
        respondSuccessHandler = messageFromServerBroadcaster
                .whenReceive(MessageType.RESPOND_SUCCESS).listen(m -> {
                    if (!(m instanceof Respond)) return;
                    removeHandlers();
                    callback.onSuccess((Respond) m);
                });
        respondFailHandler = messageFromServerBroadcaster
                .whenReceive(MessageType.RESPOND_FAIL).listen(m -> {
                    if (!(m instanceof Respond)) return;
                    removeHandlers();
                    callback.onError(((Respond) m).getResult());
                });
    }

    public void run(Message<MessageType> request, HandlerCallback<Respond> callback) {
        this.callback = callback;
        if (!messageFromServerBroadcaster.isConnecting()) {
            callback.onError(new CommunicationIOException());
            return ;
        }
        try {
            messageToServerSender.send(request);
        } catch (IOException e) {
            removeHandlers();
            callback.onError(new CommunicationIOException("Error when communicate with server", e));
        }
    }

    private void removeHandlers() {
        messageFromServerBroadcaster.onError.removeListener(errorHandler);
        messageFromServerBroadcaster.whenReceive(MessageType.RESPOND_SUCCESS).removeListener(respondSuccessHandler);
        messageFromServerBroadcaster.whenReceive(MessageType.RESPOND_FAIL).removeListener(respondFailHandler);
    }
}
