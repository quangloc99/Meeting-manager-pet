package ru.ifmo.se.s267880.lab56.commandControllerHelper;

import java.lang.annotation.*;

/**
 * An annotation that contains the usage of a command.
 *
 * @author Tran Quang Loc
 * @see Command
 * @see ReflectionCommandAdder
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Usage {
    /**
     * The usage of a command.
     */
    String value();
}
