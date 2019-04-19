package ru.ifmo.se.s267880.lab56.shared.communication;

import ru.ifmo.se.s267880.lab56.shared.Meeting;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CommandExecuteRespond implements Serializable {
    private CommandExecuteRespondStatus status;
    private Serializable result;
    private List<Meeting> collection;
    private transient boolean initialized;

    public CommandExecuteRespond(CommandExecuteRespondStatus status, Serializable result, List<Meeting> collection) {
        this.initialized = true;
        Objects.requireNonNull(status);
        this.status = status;
        this.result = result;
        this.collection = collection;
    }

    public CommandExecuteRespondStatus getStatus() {
        return this.status;
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
}
