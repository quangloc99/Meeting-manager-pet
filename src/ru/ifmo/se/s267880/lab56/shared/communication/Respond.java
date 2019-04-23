package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.Meeting;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;

public class Respond implements Serializable, Message<MessageType> {
    private MessageType respondType;
    private Serializable result;
    private List<Meeting> collection;
    private transient boolean initialized;

    @Override
    public MessageType getType() { return respondType; }

    public Respond(MessageType respondType, Serializable result, List<Meeting> collection) {
        this.initialized = true;
        if (respondType != MessageType.RESPOND_FAIL && respondType != MessageType.RESPOND_SUCCESS) {
            throw new IllegalArgumentException("respond type must be RESPOND_SUCCESS or RESPOND_FAIL");
        }
        this.respondType = respondType;
        this.result = result;
        this.collection = collection;
    }

    public <T extends Serializable> T getResult() {
        if (this.initialized) {
            throw new RuntimeException("Cannot get result of this object, which is initialized with constructor.");
        }
        return (T)this.result;
    }

    public List<Meeting> getCollection() {
        if (this.initialized) {
            throw new RuntimeException("Cannot get result of this object, which is initialized with constructor.");
        }
        return collection;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void afterSent(Sender sender) throws IOException {
        if (!initialized) throw new RuntimeException("This method must be call at the sender side.");
        if (result instanceof Message)
            ((Message) result).afterSent(sender);
    }

    @Override
    public void afterReceived(Receiver receiver) throws IOException {
        if (initialized) throw new RuntimeException("This method must be call at the receiver side.");
        if (result instanceof Message)
            ((Message) result).afterReceived(receiver);
    }
}
