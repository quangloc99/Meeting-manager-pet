package ru.ifmo.se.s267880.lab56.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ResultToClient implements Serializable {
    private ResultToClientStatus status;
    private Serializable result;
    private List<Meeting> collection;
    private transient boolean initialized = false;

    public ResultToClient(ResultToClientStatus status, Serializable result, List<Meeting> collection) {
        this.initialized = true;
        Objects.requireNonNull(status);
        this.status = status;
        this.result = result;
        this.collection = collection;
    }

    public ResultToClientStatus getStatus() {
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
