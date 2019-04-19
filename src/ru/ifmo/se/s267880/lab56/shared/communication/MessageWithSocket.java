package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

public interface MessageWithSocket extends Serializable {
    void afterSent(Socket socket) throws IOException;
    void afterReceived(Socket socket) throws IOException;
}
