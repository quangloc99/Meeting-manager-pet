package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.Helper;

import java.io.IOException;
import java.util.*;

/**
 * A class for reading user input (both command and arguments) and execute the correspond command.
 * It look like a Commands manager, but the commands's implementations will be independent to this class.
 * Because of the scope of this lab, and the limitation of Google's gson library (and other's too, because they did not designed
 * to read input from stdin), this class only manages commands with the number of arguments not more than 1. But still
 * this class can be extends more.
 * @author Tran Quang Loc
 */
abstract public class CommandController {
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
        public ErrorWhileRunningCommand(String command, Exception error) {
            super(String.format("An Error orcured while running command \"%s\": %s", command, error.getMessage()));
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

    public static class NeedMoreInputException extends Exception { }

    protected Map<String, Handler> commandHandlers = new TreeMap<>();  // so the commands can be alphabetized

    private int nInputLimit = 10;

    /**
     * Add a command with no argument.
     * If there is not enough argument for the handler to process, the handler must throw {@link NeedMoreInputException}.
     * If the input are in the wrong order or it cannot be cast/process, the handler must throw {@link IncorrectInputException}.
     * Inorder to avoid infinity request for input, {@link #nInputLimit} - maximum number of inputs per command - is set to 10 by default.
     * But it can be config easily using the setter.
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
    public Object execute() throws CommandNotFoundException, IncorrectInputException, Exception {
        String userCommand = getUserCommand();
        if (!commandHandlers.containsKey(userCommand)) {
            throw new CommandNotFoundException(userCommand);
        }

        Handler handler = commandHandlers.get(userCommand);
        List<Object> argList = new LinkedList<>();
        boolean isExecuting = true;
        for (int i = 0; i <= nInputLimit && isExecuting; ++i) {
            try {
                return handler.process(argList.toArray());
            } catch (NeedMoreInputException e) {
                argList.add(getUserInput());
            } catch (IncorrectInputException e) {
                if (e.getMessage().isEmpty()) {
                    throw new IncorrectInputException(userCommand, argList.toArray());
                }
                throw e;  // rethrow
            } catch (Exception e) {
                e.printStackTrace();
                throw new ErrorWhileRunningCommand(userCommand, e);
            }
        }
        throw new IncorrectInputException(userCommand, argList.toArray());
    }

    /**
     * Get the number of input limit.
     */
    public int getNInputLimit() {
        return nInputLimit;
    }

    /**
     * Set the number of input limit.
     */
    public void setNInputLimit(int nInputLimit) {
        this.nInputLimit = nInputLimit;
    }

    /**
     * Get user command.
     */
    abstract protected String getUserCommand() throws IOException;

    /**
     * Get user input.
     */
    abstract protected Object getUserInput() throws IOException;
}
