package ru.ifmo.se.s267880.lab56.client.commandHandlers;

import ru.ifmo.se.s267880.lab56.shared.commandsController.helper.CommandHandlers;
import ru.ifmo.se.s267880.lab56.shared.communication.*;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

abstract public class ClientCommandsHandlers implements CommandHandlers {
    private String commandName = null;
    private Object[] commandParams;
    private Supplier<CommandToServerExecutor> commandExecutorSupplier;

    public ClientCommandsHandlers(Supplier<CommandToServerExecutor> commandExecutorSupplier) {
        this.commandExecutorSupplier = commandExecutorSupplier;
    }

    @Override
    public void setCommandInformation(String name, Object[] args) {
        commandName = name;
        commandParams = args;
    }

    @Override
    public String getCommandName() { return commandName; }

    @Override
    public Object[] getCommandParams() { return commandParams; }

    public CommandExecuteRequest generateRequest() {
        return new CommandExecuteRequest(getCommandName(), Arrays.stream(getCommandParams())
                .map(o -> (Serializable) o)
                .toArray(Serializable[]::new)
        );
    }

    public CommandToServerExecutor buildCommandExecutor() {
        return commandExecutorSupplier.get();
    }
}
