package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

public interface MessageWithSocketChannel extends Serializable {
    void afterSent(SocketChannel socketChannel) throws IOException;
    void afterReceived(SocketChannel socketChannel) throws IOException;
}
