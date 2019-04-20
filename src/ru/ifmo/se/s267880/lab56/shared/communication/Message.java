package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.*;

public interface Message<MessageType> extends Serializable {
    void afterSent(Sender sender) throws IOException;
    void afterReceived(Receiver receiver) throws IOException;
    default MessageType getType() { return null; }
}
