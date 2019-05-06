package ru.ifmo.se.s267880.lab56.shared.commandsController;

/**
 * An exception that will be thrown when there is no command.
 * @author Tran Quang Loc
 */
public class CommandNotFoundException extends Exception {
    private String commandName;
    public CommandNotFoundException(String commandName) {
        super("Command \"" + commandName + "\" not found.");
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }
}
