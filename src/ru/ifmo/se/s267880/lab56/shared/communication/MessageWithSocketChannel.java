package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.Helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

public interface MessageWithSocketChannel extends Serializable {
    void afterSent(SocketChannel socketChannel) throws IOException;
    void afterReceived(SocketChannel socketChannel) throws IOException;

    default void send(SocketChannel socketChannel) throws IOException {
        ByteBuffer bf = ByteBuffer.wrap(Helper.serializableToByteArray(this));
        socketChannel.write(bf);
        this.afterSent(socketChannel);
    }

    static <T extends MessageWithSocketChannel> T receive(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(Channels.newInputStream(socketChannel));
        @SuppressWarnings("unchecked")
        T res = (T) in.readObject();
        res.afterReceived(socketChannel);
        return res;
    }
}
