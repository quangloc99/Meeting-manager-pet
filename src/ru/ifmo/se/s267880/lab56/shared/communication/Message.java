package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.*;

public interface Message<MessageType> extends Serializable {
    default void afterSent(Sender sender) throws IOException {}
    default void afterReceived(Receiver receiver) throws IOException {}
    default MessageType getType() { return null; }
}
