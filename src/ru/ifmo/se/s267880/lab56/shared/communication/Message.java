package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.Helper;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public interface Message<MessageType> extends Serializable {
    void afterSent(Sender sender) throws IOException;
    void afterReceived(Receiver receiver) throws IOException;
    default MessageType getType() { return null; }

    default void send(Sender sender) throws IOException {
        sendWithStream(sender);
    }

    static <T extends Message> T receive(Receiver receiver) throws IOException, ClassNotFoundException {
        return receiveWithStream(receiver);
    }

    default void sendWithStream(Sender sender) throws IOException {
        OutputStream out = sender.getOutputStream();
        new ObjectOutputStream(out).writeObject(this);
        out.flush();
        afterSent(sender);
    }

    default void sendWithChannel(Sender sender) throws IOException {
        ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(this));
        sender.getWritableByteChannel().write(bf);
        this.afterSent(sender);
    }

    static <T extends Message> T receiveWithStream(Receiver receiver) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        T res = (T) new ObjectInputStream(receiver.getInputStream()).readObject();
        res.afterReceived(receiver);
        return res;
    }

    static <T extends Message> T receiveWithChannel(Receiver receiver) throws IOException, ClassNotFoundException {
        return receiveWithStream(receiver);   // still has no better way to do this.
    }
}
