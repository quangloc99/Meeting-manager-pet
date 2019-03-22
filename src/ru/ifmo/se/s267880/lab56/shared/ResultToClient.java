package ru.ifmo.se.s267880.lab56.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ResultToClient<T extends Serializable> implements Serializable {
    private ResultToClientStatus status;
    private List<T> collection;
    private transient boolean initialized = false;

    public ResultToClient(ResultToClientStatus status, List<T> collection) {
        this.initialized = true;
        Objects.requireNonNull(status);
        Objects.requireNonNull(collection);
        collection.forEach(Objects::requireNonNull);
        this.status = status;
        this.collection = new LinkedList<T>();
        this.collection.addAll(collection);
    }

    public ResultToClient(ResultToClientStatus status, List<T> collection, Comparator<T> comptor) {
        this(status, collection);
        this.collection.sort(comptor);
    }

    public ResultToClientStatus getStatus() {
        return this.status;
    }

    public List<T> getCollection() {
        if (this.initialized) {
            throw new RuntimeException("Cannot get parameters of this object because it is initialized with constructor.");
        }
        return this.collection;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
