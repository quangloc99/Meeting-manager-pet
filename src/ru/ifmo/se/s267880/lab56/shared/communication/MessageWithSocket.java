package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.*;
import java.net.Socket;

public interface MessageWithSocket extends Serializable {
    void afterSent(Socket socket) throws IOException;
    void afterReceived(Socket socket) throws IOException;

    default void send(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        new ObjectOutputStream(out).writeObject(this);
        out.flush();
        afterSent(socket);
    }

    static <T extends MessageWithSocket> T receive(Socket socket) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        T res = (T) new ObjectInputStream(socket.getInputStream()).readObject();
        res.afterReceived(socket);
        return res;
    }
}
