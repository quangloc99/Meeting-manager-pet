package ru.ifmo.se.s267880.lab5.commandControllerHelper;

import java.lang.annotation.*;

import ru.ifmo.se.s267880.lab5.CommandController;

/**
 * An annotation mark a method to be a commands.
 *
 * The object that has methods with this annotation can be passed to {@link ReflectionCommandAdder#addCommand(CommandController, Object, InputPreprocessor)}
 *
 * @Author Tran Quang Loc
 * @see ReflectionCommandAdder
 * @see Usage
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /**
     * The name of the command. Default is an empty string, and {@link ReflectionCommandAdder} will use the method's name
     * instead.
     */
    String value() default "";
}
