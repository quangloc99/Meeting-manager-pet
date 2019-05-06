package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.Helper;

/**
 * An Exception that will be thrown when the input for a command is incorrect.
 */
public class IncorrectInputException extends Exception {
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

