package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import java.lang.annotation.*;

/**
 * An annotation that contains the usage of a command.
 *
 * @author Tran Quang Loc
 * @see Command
 * @see ReflectionCommandAdder
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Usage {
    /**
     * The usage of a command.
     */
    String value();

    String[] params() default {};
}
