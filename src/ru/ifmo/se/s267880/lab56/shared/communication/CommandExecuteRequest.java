package ru.ifmo.se.s267880.lab56.shared.communication;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collections;

/**
 * A query to the server.
 * Note that if this object is initialized with constructor (that is, it is not sent to the server), method
 * CommandExecuteRequest#getParameters() will throw RuntimeException for immutability purpose.
 */
public class CommandExecuteRequest implements Serializable {
    private String commandName;
    private List<Serializable> parameters;
    private transient boolean initialized;

    /**
     * @param commandName - the commandName of the query
     * @param args - list of parameters that will be sent to the server.
     */
    public CommandExecuteRequest(String commandName, Serializable[] args) {
        this.initialized = true;

        this.commandName = commandName;
        Objects.requireNonNull(commandName);

        this.parameters = Arrays.asList(args);
    }

    public String getCommandName() {
        return commandName;
    }

    public List<Object> getParameters() {
        if (initialized) {
            throw new RuntimeException("Cannot get parameters of this object because it is initialized with constructor.");
        }
        return Collections.unmodifiableList(parameters);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
