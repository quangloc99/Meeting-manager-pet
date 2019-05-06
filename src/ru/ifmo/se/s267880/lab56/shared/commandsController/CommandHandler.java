package ru.ifmo.se.s267880.lab56.shared.commandsController;

import ru.ifmo.se.s267880.lab56.shared.functional.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.functional.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The base interface for Handler
 */
public interface CommandHandler {
    void process(Object[] args, HandlerCallback<Object> callback);
    default String getUsage(String format, String commandName) {
        return String.format(format, commandName, "This command has no usage");
    }

    default String getUsage(String commandName) { return getUsage("%s  %s", commandName); }

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

    static CommandHandler ofFunction(FunctionWithException<Object[], Object> fn) {
        return (args, callback) -> {
            try {
                callback.onSuccess(fn.apply(args));
            } catch (Exception e) {
                callback.onError(e);
            }
        };
    }

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
