package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

/**
 * An interface providing a clean way to get the current command's name and parameters.
 * This interface is used with {@link ReflectionCommandHandlerGenerator}. {@link ReflectionCommandHandlerGenerator} will handle commands
 * in a dirty way, it will call {@link #setCommandInformation(String, Object...)} when invoking a commands.
 *
 * Note that inorder to unset information, {@link #setCommandInformation(String, Object...)} will call
 * {@link #setCommandInformation(String, Object...)} with the first parameter equals to null.
 */
public interface CommandHandlers {
    /**
     * Pass the current command's information from {@link ReflectionCommandHandlerGenerator} to this object.
     * Note that if name is null, then {@link ReflectionCommandHandlerGenerator} is trying to unset the information.
     *
     * @param name - name of the command.
     * @param params - the parameters will be pass into the command's handler.
     */
    default void setCommandInformation(String name, Object... params) {}
}
