package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.IOException;

public class CommunicationIOException extends IOException {
    public CommunicationIOException() {
        super();
    }

    public CommunicationIOException(String msg) {
        super(msg);
    }

    public CommunicationIOException(Throwable cause) {
        super(cause);
    }

    public CommunicationIOException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
