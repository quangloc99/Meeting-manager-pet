package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.Helper;

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
// TODO: add method "addCommand" to add command's handler without return value.
public class CommandController {
    /**
     * The base interface for Handler
     */
    public interface Handler {
        Object process(Object args[]) throws Exception;
        default String getUsage() {
            return "This command has no usage";
        }
    }

    public static class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String commandName) {
            super("Usage \"" + commandName + "\" not found. Use command \"list-commands\" for the list of usable commands.");
        }
    }

    public static class ErrorWhileRunningCommand extends Exception {
        private String commandName;
        public ErrorWhileRunningCommand(String command, Exception error) {
            super(error.getMessage(), error);
            commandName = command;
        }

        public String getCommandName() {
            return commandName;
        }
    }

    public static class IncorrectInputException extends Exception {
        public IncorrectInputException() {
            super();
        }

        public IncorrectInputException(String msg) {
            super(msg);
        }

        public IncorrectInputException(String command, Object[] objs) {
            super(String.format("Command \"%s\" can not run with input: %s", command, Helper.join(", ", objs)));
        }
    }

    private Map<String, Handler> commandHandlers = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Add a command with no argument.
     * If the input are in the wrong order or it cannot be cast/process, the handler must throw {@link IncorrectInputException}.
     *
     * @param commandName the name of the command
     * @param handler the handler for the command.
     */
    public void addCommand(String commandName, Handler handler) {
        commandHandlers.put(commandName, handler);
    }

    public void addCommand(String commandName, String usage, CommandController.Handler handler) {
        addCommand(commandName, new Handler() {
            @Override
            public Object process(Object[] args) throws Exception {
                return handler.process(args);
            }

            @Override
            public String getUsage() {
                return usage;
            }
        });
    }

    /**
     * Get the command's handler.
     * @param commandName the name of the command.
     */
    public Handler getCommandHandler(String commandName) {
        return commandHandlers.get(commandName);
    }

    /**
     * Read user input and the execute the correspond command.
     * @throws CommandNotFoundException
     * @throws Exception
     */
    public void execute(String userCommand, List<Object> argList, HandlerCallback<Object> callback) {
        try {
            if (!commandHandlers.containsKey(userCommand)) {
                throw new CommandNotFoundException(userCommand);
            }
            try {
                Handler handler = commandHandlers.get(userCommand);
                Object res = handler.process(argList.toArray());
                callback.onSuccess(res);
            } catch (IncorrectInputException e) {
                if (e.getMessage().isEmpty()) {
                    throw new IncorrectInputException(userCommand, argList.toArray());
                }
                throw e;
            } catch (Exception e) {
                throw new ErrorWhileRunningCommand(userCommand, e);
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public Map<String, Handler> getCommandHandlers() {
        return Collections.unmodifiableMap(commandHandlers);
    }
}
