package ru.ifmo.se.s267880.lab56;

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
    public static final int SUCCESS = 0;
    public static final int NEED_MORE_INPUT = 1;
    public static final int FAIL = 2;

    /**
     * The base interface for Handler
     */
    public interface Handler {
        int process(Object args[]) throws Exception;
    }

    public class CommandNotFoundException extends Exception {
        public CommandNotFoundException(String commandName) {
            super("Usage \"" + commandName + "\" not found. Use command \"list-commands\" for the list of usable commands.");
        }
    }

    public class ErrorWhileRunningCommand extends Exception {
        public ErrorWhileRunningCommand(String command, Exception error) {
            super(String.format("An Error orcured while running command \"%s\": %s", command, error.getMessage()));
        }
    }

    public class IncorrectInput extends Exception {
        public IncorrectInput(String command, Object[] objs) {
            super(String.format("Command \"%s\" can not run with input: %s", command, Helper.join(", ", objs)));
        }
    }

    protected Map<String, Handler> commandHandlers = new TreeMap<>();  // so the commands can be alphabetized

    private int nInputLimit = 10;

    /**
     * Add a command with no argument.
     * <p>
     * A lambda can be passed into this method with the following form:
     * <pre>{@code
     * (Object[] args) -> {
     *     return <<status>>;
     * }
     * }
     * </pre>
     * </p>
     *
     * <p>
     * The {@code <<status>>} here is one of the following value: <ul>
     *     <li>{@link #SUCCESS}: when the command is successfully executed.</li>
     *     <li>{@link #FAIL}: when the command is fail to executed.</li>
     *     <li>{@link #NEED_MORE_INPUT}: ask for more input. The handler will be called again with one more input.</li>
     * </ul>
     * </p>
     *
     * Inorder to avoid infinity request for input, {@link #nInputLimit} - maximum number of inputs per command - is set to 10 by default.
     * But it can be config easily using the setter.
     *
     * @param commandName the name of the command
     * @param handler the handler for the command.
     */
    public void addCommand(String commandName, Handler handler) {
        commandHandlers.put(commandName, handler);
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
    public void execute() throws CommandNotFoundException, IncorrectInput, Exception {
        String userCommand = getUserCommand();
        if (!commandHandlers.containsKey(userCommand)) {
            throw new CommandNotFoundException(userCommand);
        }

        Handler handler = commandHandlers.get(userCommand);
        List<Object> argList = new LinkedList<>();
        boolean isExecuting = true;
        for (int i = 0; i < nInputLimit && isExecuting; ++i) {
            try {
                int status = handler.process(argList.toArray());
                switch (status) {
                    case CommandController.SUCCESS:
                        isExecuting = false;
                        break;
                    case CommandController.NEED_MORE_INPUT:
                        argList.add(getUserInput());
                        break;
                    case CommandController.FAIL:
                        throw new IncorrectInput(userCommand, argList.toArray());
                }
            } catch (IncorrectInput e) {
                throw e;  // rethrow
            } catch (Exception e) {
                throw new ErrorWhileRunningCommand(userCommand, e);
            }
        }
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
    abstract protected String getUserCommand();

    /**
     * Get user input.
     */
    abstract protected Object getUserInput();
}
