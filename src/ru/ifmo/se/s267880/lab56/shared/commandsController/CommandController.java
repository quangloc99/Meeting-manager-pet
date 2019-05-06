package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;

import java.util.*;
import java.util.List;

/**
 * A class for reading user input (both command and arguments) and execute the correspond command.
 * It look like a Commands manager, but the commands's implementations will be independent to this class.
 * Because of the scope of this lab, and the limitation of Google's GSON library (and other's too, because they did not designed
 * to read input from stdin), this class only manages commands with the number of arguments not more than 1. But still
 * this class can be extends more.
 * @author Tran Quang Loc
 */
public class CommandController {
    private Map<String, CommandHandler> commandHandlers = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Add a command with no argument.
     * If the input are in the wrong order or it cannot be cast/process, the handler must throw {@link IncorrectInputException}.
     *
     * @param commandName the name of the command
     * @param handler the handler for the command.
     */
    public void addCommand(String commandName, CommandHandler handler) {
        commandHandlers.put(commandName, handler);
    }

    /**
     * Read user input and the execute the correspond command.
     * @throws CommandNotFoundException
     * @throws Exception
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
     * Get the command's handler.
     * @param commandName the name of the command.
     */
    public CommandHandler getCommandHandler(String commandName) {
        return commandHandlers.get(commandName);
    }

    public Map<String, CommandHandler> getCommandHandlers() {
        return Collections.unmodifiableMap(commandHandlers);
    }
}
