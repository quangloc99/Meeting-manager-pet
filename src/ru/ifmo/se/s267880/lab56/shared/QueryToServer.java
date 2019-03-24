package ru.ifmo.se.s267880.lab56.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collections;

/**
 * A query to the server.
 * Note that if this object is initialized with constructur (that is, it is not sented to the server), method
 * QueryToServer#getParameters() will throw RuntimeException for immutation purpose.
 */
public class QueryToServer implements Serializable {
    private String name;
    private List<Serializable> parameters;
    private transient boolean initialized = false;

    /**
     * @param name - the name of the query
     * @param args - list of parameters that will be sent to the server.
     */
    public QueryToServer(String name, Serializable[] args) {
        this.initialized = true;

        this.name = name;
        Objects.requireNonNull(name);

        this.parameters = Arrays.asList(args);
    }

    public String getName() {
        return name;
    }

    public List<Serializable> getParameters() {
        if (initialized) {
            throw new RuntimeException("Cannot get parameters of this object because it is initialized with constructor.");
        }
        return Collections.unmodifiableList(parameters);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
