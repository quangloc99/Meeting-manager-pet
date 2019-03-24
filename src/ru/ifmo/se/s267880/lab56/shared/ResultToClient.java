package ru.ifmo.se.s267880.lab56.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ResultToClient implements Serializable {
    private ResultToClientStatus status;
    private Serializable result;
    private transient boolean initialized = false;

    public ResultToClient(ResultToClientStatus status, Serializable result) {
        this.initialized = true;
        Objects.requireNonNull(status);
        this.status = status;
        this.result = result;
    }

    public ResultToClientStatus getStatus() {
        return this.status;
    }

    public Object getResult() {
        if (this.initialized) {
            throw new RuntimeException("Cannot get parameters of this object because it is initialized with constructor.");
        }
        return this.result;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
