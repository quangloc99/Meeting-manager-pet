package ru.ifmo.se.s267880.lab56.shared.commandsController.helper;

import ru.ifmo.se.s267880.lab56.shared.commandsController.CommandHandler;

import java.util.Map;

/**
 * An interface providing a clean way to get the current command's name and parameters.
 * This interface is used with {@link ReflectionCommandHandlerGenerator}. {@link ReflectionCommandHandlerGenerator} will handle commands
 * in a dirty way, it will call {@link #setCommandInformation(String, Object...)} when invoking a commands.
 *
 * Note that in order to unset information, {@link #setCommandInformation(String, Object...)} will call
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

    /**
     * Getter for the current command's name. This default method return an empty string.
     */
    default String getCommandName() { return ""; }

    /**
     * Getter for the current parameters. This default method return null.
     */
    default Object[] getCommandParams() { return null; }

    /**
     * Generate command handlers. This command should call {@link ReflectionCommandHandlerGenerator#generate(Class, CommandHandlers, InputPreprocessor)}
     * in order to generate handlers. This method should be use over {@link ReflectionCommandHandlerGenerator#generate(Class, CommandHandlers, InputPreprocessor)}
     * because beside being the wrapper for that static function, it can also be extended in the children class.
     *
     * Note: in order to merge the map from super and the new map, {@link Map#putAll(Map)} should not be used.
     * Instead, it should be done with {@link CommandHandler#join(CommandHandler...)} like the following:
     * <pre>
     *     //....
     *      superHandlers = super.generateHandlers(preprocessor);
     *      newHandlers = ReflectionCommandHandlerGenerator.generate(TheClass.class, this, preprocessor);
     *      newHandlers.forEach((commandName, handler) -> superHandlers.merge(commandName, handler, CommandHandler::join));
     *      return superHandlers;
     * </pre>
     * @param preprocessor the input preprocessor.
     * @return a map between the command's name and its handler.
     */
    Map<String, CommandHandler> generateHandlers(InputPreprocessor preprocessor);
}
