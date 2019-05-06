package ru.ifmo.se.s267880.lab56.shared.commandsController;

/**
 * An exception that will be thrown when there is an Exception while executing a command.
 */
public class ErrorWhileRunningCommandException extends Exception {
    private String commandName;
    public ErrorWhileRunningCommandException(String command, Exception error) {
        super(error.getMessage(), error);
        commandName = command;
    }

    public String getCommandName() {
        return commandName;
    }
}
