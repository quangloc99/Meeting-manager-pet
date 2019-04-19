package ru.ifmo.se.s267880.lab56.shared.commandsController;

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
