package ru.ifmo.se.s267880.lab5.commandControllerHelper;

/**
 * An exeption that will be thrown when a {@link InputPreprocessor} cannot preprocess the input.
 * @author Tran Quang Loc
 */
public class CannotPreprocessInputException extends Exception {
    public CannotPreprocessInputException(String msg) {
        super(msg);
    }
}

