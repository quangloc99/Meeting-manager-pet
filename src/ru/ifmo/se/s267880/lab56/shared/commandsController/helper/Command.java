package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import java.lang.annotation.*;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandController;

/**
 * An annotation mark a method to be a commands.
 *
 * The object that has methods with this annotation can be passed to {@link ReflectionCommandAdder#addCommand(CommandController, Object, InputPreprocessor)}
 *
 * @Author Tran Quang Loc
 * @see ReflectionCommandAdder
 * @see Usage
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The name of the command. Default is an empty string, and {@link ReflectionCommandAdder} will use the method's name
     * instead.
     */
    String value() default "";

    /**
     * Mark this field if the method is an additional command (not in the task).
     */
    boolean additional() default false;
}
