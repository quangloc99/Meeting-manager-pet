package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;

import java.util.*;
import java.util.List;

/**
 * A class for managing all the commands. It has ability to add commands, remove commands and execute commands with
 * given command name, parameters.
 * @author Tran Quang Loc
 */
public class CommandController {
    private Map<String, CommandHandler> commandHandlers = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Add a command.
     *
     * @param commandName the name of the command
     * @param handler the handler for the command.
     */
    public void addCommand(String commandName, CommandHandler handler) {
        commandHandlers.put(commandName, handler);
    }

    /**
     * Execute the command with a given command name and parameters list.
     * @param userCommand the command name.
     * @param argList the arguments/parameters  list.
     * @param callback a callback that will be called when the command is done executing or for handling error.
     */
    public void execute(String userCommand, List<Object> argList, HandlerCallback<Object> callback) {
        if (!commandHandlers.containsKey(userCommand)) {
            callback.onError(new CommandNotFoundException(userCommand));
            return ;
        }
        CommandHandler handler = commandHandlers.get(userCommand);
        handler.process(argList.toArray(), new HandlerCallback<>(callback::onSuccess,
                e -> {
                    if (e instanceof  IncorrectInputException) {
                        if (e.getMessage() == null || e.getMessage().isEmpty()) {
                            e = new IncorrectInputException(userCommand, argList.toArray());
                        }
                    } else {
                        e = new ErrorWhileRunningCommandException(userCommand, e);
                    }
                    callback.onError(e);
                }
        ));
    }

    /**
     * Get the command's handler by its name.
     * @param commandName the name of the command.
     */
    public CommandHandler getCommandHandler(String commandName) {
        return commandHandlers.get(commandName);
    }

    /**
     * Get all the command handlers.
     */
    public Map<String, CommandHandler> getCommandHandlers() {
        return Collections.unmodifiableMap(commandHandlers);
    }
}
