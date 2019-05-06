package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.functional.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The base interface for a command Handler. It come with utilities function to create a handler more easier.
 * @author Tran Quang Loc
 */
public interface CommandHandler {
    /**
     * The "body" of the handler. This method will be call when handling a command.
     * @param args the arguments list.
     * @param callback a callback that need to be call when the command is done executing or if there is an error.
     */
    void process(Object[] args, HandlerCallback<Object> callback);
    default String getUsage(String format, String commandName) {
        return String.format(format, commandName, "This command has no usage");
    }

    /**
     * Get the usage of the command.
     * @param commandName the command name.
     * @return
     */
    default String getUsage(String commandName) { return getUsage("%s  %s", commandName); }

    /**
     * Set the command handler with usage. The main purpose of this method is to use with lambda expression.
     * @param usage the usage of the command.
     * @param handler the handler of the command.
     * @return the handler with the new usage.
     */
    static CommandHandler withUsage(String usage, CommandHandler handler) {
        return new CommandHandler() {
            @Override
            public void process(Object[] args, HandlerCallback<Object> callback) {
                handler.process(args, callback);
            }

            @Override
            public String getUsage(String format, String commandName) {
                return String.format(format, commandName, usage);
            }
        };
    }

    /**
     * Convert a function with exception into a command handler.
     */
    static CommandHandler ofFunction(FunctionWithException<Object[], Object> fn) {
        return (args, callback) -> {
            try {
                callback.onSuccess(fn.apply(args));
            } catch (Exception e) {
                callback.onError(e);
            }
        };
    }

    /**
     * Convert a function with exception into a handler with usage.
     */
    static CommandHandler ofFunction(String usage, FunctionWithException<Object[], Object> fn) {
        return new CommandHandler() {
            @Override
            public void process(Object[] args, HandlerCallback<Object> callback) {
                try {
                    callback.onSuccess(fn.apply(args));
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public String getUsage(String format, String commandName) {
                return String.format(format, commandName, usage);
            }
        };
    }

    /**
     * Convert a consumer with exception into a handler.
     */
    static CommandHandler ofConsumer(ConsumerWithException<Object[]> fn) {
        return (args, callback) -> {
            try {
                fn.accept(args);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        };
    }

    /**
     * Convert a consumer with exception into a handler.
     */
    static CommandHandler ofConsumer(String usage, ConsumerWithException<Object[]> fn)  {
        return new CommandHandler() {
            @Override
            public void process(Object[] args, HandlerCallback<Object> callback) {
                try {
                    fn.accept(args);
                    callback.onSuccess(null);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public String getUsage(String format, String commandName) {
                return String.format(format, commandName, usage);
            }
        };
    }

    /**
     * Join 2 or more handler into a handler. The handler will be tried calling. If the handler run successfully, then
     * the new handler stop and return the result. Otherwise it will run until there is no more handler. In that case
     * the handlers_::onError method will be triggered.
     * This method also join the usage, and separate them by EOL character.
     * @param handlers_ the list of the handlers.
     * @return a new handler that combines all the handlers_
     */
    static CommandHandler join(CommandHandler... handlers_) {
        assert(handlers_.length > 0);
        List<CommandHandler> handlers = Arrays.asList(handlers_);
        return new CommandHandler() {
            @Override
            public void process(Object[] args, HandlerCallback<Object> callback) {
                Iterator<CommandHandler> it = handlers.iterator();
                HandlerCallback<Object> cb = new HandlerCallback<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        callback.onSuccess(o);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (e instanceof IncorrectInputException) {
                            if (it.hasNext()) {
                                it.next().process(args, this);
                                return ;
                            }
                        }
                        callback.onError(e);
                    }
                };
                it.next().process(args, cb);   // because the length is bigger than 0.
            }

            @Override
            public String getUsage(String format, String commandName) {
                return handlers.stream()
                        .map(h -> h.getUsage(format, commandName))
                        .collect(Collectors.joining("\n"));
            }
        };
    }
}
